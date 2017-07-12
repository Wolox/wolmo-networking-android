/**
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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import ar.com.wolox.wolmo.networking.optimizations.BaseCallCollapser;
import ar.com.wolox.wolmo.networking.optimizations.ICallCollapser;
import ar.com.wolox.wolmo.networking.retrofit.callback.NetworkCallback;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Handles the caching retrieval of a query and success/error notification regarding that operation.
 * <p/>
 * The main method is {@link #query} that, provided of a policy, the necessary information to
 * retrieve data and notify the user, determines the action to take and notifies accordingly.
 */
public final class Repository<T, C> {

    public static final int INTERNAL_ERROR_CODE = 666;
    public static final int CACHE_MISS_ERROR_CODE = 888;

    /**
     * Flags for cache interaction control.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_NONE, CACHE_FIRST, CACHE_ONLY, TIME_RESOLVE})
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
     * Checks whether the refresh delta time has passed.
     * If it has, it refreshes cache by getting data from API. Else, it returns cache data.
     * </p>
     * For example, if the field returns 2000, then 2 seconds have to elapse before the data
     * is considered 'dirty'.
     */
    public static final int TIME_RESOLVE = 4;

    /**
     * Default refresh delta time. Users can modify it to set the default refresh delta time for
     * future created instances of {@link Repository}.
     */
    public static long DEFAULT_REFRESH_DELTA_TIME = TimeUnit.DAYS.toMillis(1);

    /**
     * Default {@link AccessPolicy}. Users can modify it to set the defalt policy for future
     * instances of {@link Repository}.
     */
    public static @AccessPolicy int DEFAULT_ACCESS_POLICY = TIME_RESOLVE;

    private static ICallCollapser CALL_COLLAPSER_INSTANCE = new BaseCallCollapser();

    private final C mCache;
    private final QueryStrategy<T, C> mQueryStrategy;
    private final @AccessPolicy int mDefaultAccessPolicy;
    private final long mRefreshDeltaInMillis;
    private final ICallCollapser mCallCollapser;

    private long mLastRefreshMoment;

    /**
     * Creates a repository.
     * <p/>
     * Users of this class must use {@link Builder} to instantiate it.
     * @param cache to query for cached items
     * @param queryStrategy to use when interacting with cache
     * @param defaultAccessPolicy that determines default interaction with cache
     * @param refreshDeltaInMillis Time interval up-to-date by the {@link #TIME_RESOLVE} policy.
     */
    private Repository(@NonNull C cache, @NonNull QueryStrategy<T, C> queryStrategy,
                       @AccessPolicy int defaultAccessPolicy,
                       @IntRange(from = 1) long refreshDeltaInMillis) {
        mCache = cache;
        mQueryStrategy = queryStrategy;
        mDefaultAccessPolicy = defaultAccessPolicy;
        mRefreshDeltaInMillis = refreshDeltaInMillis;
        mCallCollapser = CALL_COLLAPSER_INSTANCE;
        mLastRefreshMoment = System.currentTimeMillis();
    }

    /**
     * Queries the corresponding information provider, either network or cache, in order to retrieve
     * information and handle it to the user.
     * <p/>
     * The decisions regarding the interaction with the network and/or cache are governed by the
     * {@link AccessPolicy} given. Check their description for proper usage.
     *
     * @param policy policy to use for the query
     * @param call request that retrieves asked information
     * @param callback that notifies the result of the query
     */
    public void query(@AccessPolicy int policy, @NonNull Call<T> call,
                      @NonNull IRepositoryCallback<T> callback) {
        if (accessCache(policy)) {
            T cachedData = mQueryStrategy.read(mCache);
            if (cachedData != null) {
                if (shouldInvalidateCache(policy)) {
                    mQueryStrategy.invalidate(mCache);
                } else {
                    callback.onSuccess(cachedData);
                    return;
                }
            } else if (policy == CACHE_ONLY) {
                callback.onError(CACHE_MISS_ERROR_CODE);
                return;
            }
        }

        fetchData(call, callback);
    }

    /**
     * Queries the corresponding information provider, either network or cache, in order to retrieve
     * information and handle it to the user with the default {@link AccessPolicy}.
     *
     * @param call request that retrieves asked information
     * @param callback that notifies the result of the query
     */
    public void query(@NonNull Call<T> call, @NonNull IRepositoryCallback<T> callback) {
        query(mDefaultAccessPolicy, call, callback);
    }

    /**
     * @param policy policy to check
     * @return wether the policy indicates that the query should access the cache.
     */
    private boolean accessCache(@AccessPolicy int policy) {
        return policy != CACHE_NONE;
    }

    /**
     * Sets {@link #mLastRefreshMoment} to the current moment in time.
     */
    private void updateRefreshMoment() {
        mLastRefreshMoment = System.currentTimeMillis();
    }

    /**
     * @param policy policy taken
     * @return whether cached data should be invalidated.
     */
    private boolean shouldInvalidateCache(@AccessPolicy int policy) {
        return policy == TIME_RESOLVE &&
                (System.currentTimeMillis() - mLastRefreshMoment) >= mRefreshDeltaInMillis;
    }

    /**
     * Makes a request and notifies accordingly. In case of success,
     * {@link QueryStrategy#save(Object, Object)} is called to impact the change.
     *
     * @param call request to be done
     * @param callback that notifies the result of the query
     * @throws IllegalStateException if the <code>call</code> is either executed or cancelled.
     */
    private void fetchData(@NonNull Call<T> call, @NonNull final IRepositoryCallback<T> callback) {
        if (call.isExecuted() || call.isCanceled()) {
            throw new IllegalStateException("Call should be ready to use");
        }

        mCallCollapser.enqueue(call, new NetworkCallback<T>() {
            @Override
            public void onResponseSuccessful(T data) {
                mQueryStrategy.save(data, mCache);
                updateRefreshMoment();
                callback.onSuccess(data);
            }

            @Override
            public void onResponseFailed(ResponseBody responseBody, int code) {
                callback.onError(code);
            }

            @Override
            public void onCallFailure(Throwable throwable) {
                callback.onError(INTERNAL_ERROR_CODE);
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
    public static abstract class QueryStrategy<T, C> {

        /**
         * Used whenever information needs to be retrieved.
         *
         * @param cache to retrieve information from
         *
         * @return Data retrieved. Returning <code>null</code> means it was a cache miss.
         */
        @Nullable
        public abstract T read(@NonNull C cache);

        /**
         * Executed in case the cache information, in relation to a query, should be cleared.
         *
         * @param cache to invalidate data in
         */
        public abstract void invalidate(@NonNull C cache);

        /**
         * Is called into action for saving data fetched from network.
         *
         * @param data to store
         * @param cache to save data to
         */
        public abstract void save(@NonNull T data, @NonNull C cache);

    }

    /**
     * Builder for {@link Repository}.
     * <p/>
     * Users must provide a cache object of type {@link C} and a {@link QueryStrategy} at creation.
     * Every other parameter is optional.
     *
     * @see #DEFAULT_ACCESS_POLICY
     * @see #DEFAULT_REFRESH_DELTA_TIME
     *
     * @param <T> Type of elements to interact with
     * @param <C> Type of cache to handle
     */
    public static final class Builder<T, C> {

        private final C mCache;
        private final QueryStrategy<T, C> mQueryStrategy;

        private int mDefaultAccessPolicy;
        private long mRefreshDeltaInMillis;

        public Builder(@NonNull C cache, @NonNull QueryStrategy<T, C> strategy) {
            this.mCache = cache;
            this.mQueryStrategy = strategy;
            this.mDefaultAccessPolicy = DEFAULT_ACCESS_POLICY;
            this.mRefreshDeltaInMillis = DEFAULT_REFRESH_DELTA_TIME;
        }

        /**
         * Sets the default {@link AccessPolicy} for the future built instance to use.
         *
         * @param defaultAccessPolicy to set
         * @return The same instance of the {@link Builder}
         */
        public final Builder<T, C> withDefaultAccessPolicy(@AccessPolicy int defaultAccessPolicy) {
            this.mDefaultAccessPolicy = defaultAccessPolicy;
            return this;
        }

        /**
         * Sets the default refresh delta time for the future built instance to use.
         *
         * @param refreshDeltaInMillis to set
         * @return The same instance of the {@link Builder}
         */
        public final Builder<T, C> withRefreshDelta(@IntRange(from = 1) long refreshDeltaInMillis) {
            this.mRefreshDeltaInMillis = refreshDeltaInMillis;
            return this;
        }

        /**
         * @return A fully-fledged {@link Repository} with the configuration of the {@link Builder}
         *          instance
         */
        public Repository<T, C> build() {
            return new Repository<>(
                    mCache, mQueryStrategy, mDefaultAccessPolicy, mRefreshDeltaInMillis);
        }

    }

}
