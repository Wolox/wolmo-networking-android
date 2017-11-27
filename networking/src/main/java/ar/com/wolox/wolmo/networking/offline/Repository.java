/*
 * MIT License
 * <p>
 * Copyright (c) 2017 Wolox S.A
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package ar.com.wolox.wolmo.networking.offline;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ar.com.wolox.wolmo.networking.exception.CacheMissException;
import ar.com.wolox.wolmo.networking.exception.NetworkResourceException;
import ar.com.wolox.wolmo.networking.optimizations.BaseCallCollapser;
import ar.com.wolox.wolmo.networking.optimizations.ICallCollapser;
import ar.com.wolox.wolmo.networking.retrofit.callback.NetworkCallback;
import ar.com.wolox.wolmo.networking.utils.Consumer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Handles the caching retrieval of a query and success/error notification regarding that operation.
 * <p/>
 * The main method is {@link #query} that, provided of a policy, the necessary information to
 * retrieve data and notify the user, determines the action to take and notifies accordingly.
 */
public final class Repository<T, C> {

    /**
     * Flags for cache access control.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_NONE, CACHE_FIRST, CACHE_ONLY})
    public @interface AccessPolicy {}

    /**
     * Makes the queries never check cache.
     */
    public static final int CACHE_NONE = 1;

    /**
     * Makes the queries check cache first and, if missed, make a request and update it.
     */
    public static final int CACHE_FIRST = 2;

    /**
     * Makes the queries only check cache.
     */
    public static final int CACHE_ONLY = 3;

    /**
     * Default {@link AccessPolicy}. Users can modify it to set the defalt policy for future
     * instances of {@link Repository}.
     */
    public static @AccessPolicy int DEFAULT_ACCESS_POLICY = CACHE_FIRST;

    private static ICallCollapser CALL_COLLAPSER_INSTANCE = new BaseCallCollapser();

    private final C mCache;
    private final @AccessPolicy int mDefaultAccessPolicy;
    private final ICallCollapser mCallCollapser;

    /**
     * Creates a repository with a default {@link AccessPolicy}.
     * <p/>
     * @param cache to query for cached items
     * @param defaultAccessPolicy that determines default interaction with cache
     */
    public Repository(@NonNull C cache, @AccessPolicy int defaultAccessPolicy) {
        mCache = cache;
        mDefaultAccessPolicy = defaultAccessPolicy;
        mCallCollapser = CALL_COLLAPSER_INSTANCE;
    }

    /**
     * Creates a repository with {@link #DEFAULT_ACCESS_POLICY} as its policy.
     * <p/>
     * @param cache to query for cached items
     */
    public Repository(@NonNull C cache) {
        this(cache, DEFAULT_ACCESS_POLICY);
    }

    /**
     * Creates an {@link Query<T>} that when run queries the corresponding information provider,
     * either network or cache depending on the conditions, in order to retrieve information and
     * handle it to the user.
     * <p/>
     * The decisions regarding the interaction with the network and/or cache are governed by the
     * {@link AccessPolicy} given. Check their description for proper usage.
     *
     * @param policy to use for the query
     * @param call request that retrieves asked information
     * @param queryStrategy that determines how to react to local/network actions
     */
    public Query<T> query(@AccessPolicy final int policy, @NonNull final Call<T> call,
                          @NonNull final QueryStrategy<T, C> queryStrategy) {
        return new Query<T>() {
            @Override
            public void run() {
                if (!accessCache(policy)) {
                    fetchData(call, queryStrategy, this);
                    return;
                }

                T cachedData = queryStrategy.readLocalSource(mCache);
                if (cachedData != null) {
                    doOnSuccess(cachedData);
                } else if (policy == CACHE_ONLY) {
                    doOnError(new CacheMissException());
                }
            }
        };
    }

    /**
     * Same as in {@link #query(int, Call, QueryStrategy)} but using the default access policy
     * with which the instance was created.
     *
     * @param call request that retrieves asked information
     * @param queryStrategy that determines how to react to local/network actions
     *
     * @see #query(int, Call, QueryStrategy)
     */
    public Query<T> query(@NonNull Call<T> call, @NonNull final QueryStrategy<T, C> queryStrategy) {
        return query(mDefaultAccessPolicy, call, queryStrategy);
    }

    /**
     * Queries the corresponding information provider, either network or cache depending on the
     * conditions, in order to retrieve information and handle it to the user.
     * <p/>
     * Shorthand for calling {@link #query(int, Call, QueryStrategy)} and executing it immediately
     * with {@link IRepositoryCallback#onSuccess(T)} and {@link IRepositoryCallback#onError(Throwable)}
     * as its success and error callbacks.
     *
     * @param policy to use for the query
     * @param call request that retrieves asked information
     * @param queryStrategy that determines how to react to local/network actions
     * @param callback to use for notification
     *
     * @see #query(int, Call, QueryStrategy)
     */
    public void query(@AccessPolicy final int policy, @NonNull final Call<T> call,
                      @NonNull QueryStrategy<T, C> queryStrategy,
                      @NonNull final IRepositoryCallback<T> callback) {
        Query<T> repositoryQuery = query(policy, call, queryStrategy);

        repositoryQuery.onSuccess(callback::onSuccess).onError(callback::onError).run();
    }

    /**
     * Same as in {@link #query(int, Call, QueryStrategy, IRepositoryCallback)} but using the
     * default access policy with which the instance was created.
     *
     * @param call request that retrieves asked information
     * @param queryStrategy that determines how to react to local/network actions
     * @param callback to use for notification
     *
     * @see #query(int, Call, QueryStrategy, IRepositoryCallback)
     */
    public void query(@NonNull final Call<T> call, @NonNull QueryStrategy<T, C> queryStrategy,
                      @NonNull final IRepositoryCallback<T> callback) {
        query(call, queryStrategy, callback);
    }

    /**
     * @param policy policy to check
     *
     * @return wether the policy indicates that the query should access the cache.
     */
    private boolean accessCache(@AccessPolicy int policy) {
        return policy != CACHE_NONE;
    }

    /**
     * Makes a request and notifies accordingly. In case of success,
     * {@link QueryStrategy#consumeRemoteSource(Object, Object)} is called to impact the change.
     *
     * @param call request to be done
     * @param queryStrategy that determines how to react to local/network actions
     * @param repositoryQuery to notify to
     *
     * @throws IllegalStateException if the <code>call</code> is either executed or cancelled.
     */
    private void fetchData(@NonNull final Call<T> call,
                           @NonNull final QueryStrategy<T, C> queryStrategy,
                           @NonNull final Query<T> repositoryQuery) {
        if (call.isExecuted() || call.isCanceled()) {
            throw new IllegalStateException("Call should be ready to use");
        }

        mCallCollapser.enqueue(call, new NetworkCallback<T>() {
            @Override
            public void onResponseSuccessful(T data) {
                queryStrategy.consumeRemoteSource(data, mCache);
                repositoryQuery.doOnSuccess(data);
            }

            @Override
            public void onResponseFailed(ResponseBody responseBody, int code) {
                repositoryQuery.doOnError(
                        new NetworkResourceException(call.request().url().toString(), code));
            }

            @Override
            public void onCallFailure(Throwable throwable) {
                repositoryQuery.doOnError(throwable);
            }
        });
    }

    /**
     * Determines behaviour for interacting with the {@link C} cache. This is used whenever a
     * {@link #query} determines it needs talking to the cache.
     *
     * @param <T> class which is used for interacting with the {@link C} cache
     * @param <C> type of cache to use
     */
    public interface QueryStrategy<T, C> {

        /**
         * Called whenever information needs to be retrieved locally.
         *
         * @param cache to retrieve information from
         *
         * @return Data retrieved. Returning <code>null</code> means it was a cache miss.
         */
        @Nullable
        T readLocalSource(@NonNull C cache);

        /**
         * Is called into action for saving data fetched from network.
         * <p/>
         * The {@link C cache} is provided in order to be able to save the data or similar actions.
         *
         * @param data to store
         * @param cache to interact with
         */
        void consumeRemoteSource(@NonNull T data, @NonNull C cache);

    }

    /**
     * Awaits the order to execute logic of the repository query the user created. It exposes
     * {@link Consumer<T>} instances for both success and error to be notified.
     * <p/>
     * Note that calling {@link #onSuccess(Consumer)} and {@link #onError(Consumer)} is not
     * mandatory for calling {@link #run()} in case a user doesn't care about the result.
     *
     * @param <T> type of elements to process on success
     */
    public abstract static class Query<T> implements Runnable {

        private Consumer<T> successConsumer;
        private Consumer<Throwable> errorConsumer;

        private Query() {}

        /**
         * Sets the success {@link Consumer<T>}.
         *
         * @param successConsumer to use in case the query succeeds
         *
         * @return the same instance
         */
        public Query<T> onSuccess(@NonNull Consumer<T> successConsumer) {
            this.successConsumer = successConsumer;
            return this;
        }

        /**
         * Sets the error {@link Consumer<Throwable>}.
         *
         * @param errorConsumer to use in case the query fails
         *
         * @return the same instance
         */
        public Query<T> onError(@NonNull Consumer<Throwable> errorConsumer) {
            this.errorConsumer = errorConsumer;
            return this;
        }

        void doOnSuccess(T data) {
            if (successConsumer != null) successConsumer.accept(data);
        }

        void doOnError(Throwable throwable) {
            if (errorConsumer != null) errorConsumer.accept(throwable);
        }

    }
}
