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

import ar.com.wolox.wolmo.core.callback.WoloxCallback;
import ar.com.wolox.wolmo.networking.optimizations.BaseCallCollapser;
import ar.com.wolox.wolmo.networking.optimizations.ICallCollapser;
import okhttp3.Cache;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Handles the caching retrieval of a query and success/error notification regarding that operation.
 * <p/>
 * The main method is {@link #query} that, provided of a policy, the necessary information to
 * retrieve data and notify the user, determines the action to take and notifies accordingly.
 */
public abstract class Repository {

    public static final int INTERNAL_ERROR_CODE = 666;
    public static final int CACHE_MISS_ERROR_CODE = 888;

    /**
     * Flags for cache interaction control.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_NONE, CACHE_FIRST, CACHE_ONLY, TIME_RESOLVE})
    protected @interface AccessPolicy {
    }

    /**
     * Makes the queries never check cache.
     */
    protected static final int CACHE_NONE = 1;

    /**
     * Makes the queries check cache first and, if missed, make a request and update it.
     */
    protected static final int CACHE_FIRST = 2;

    /**
     * Makes the queries only check cache.
     */
    protected static final int CACHE_ONLY = 3;

    /**
     * Checks whether {@link #getRefreshDelta()} has passed. If it has, it refreshes cache by getting
     * data from API. Else, it returns cache data.
     */
    protected static final int TIME_RESOLVE = 4;

    private ICache mCache;
    private long mLastRefreshMoment;
    private ICallCollapser mCallCollapser;

    /**
     * Constructs a basic repository.
     *
     * @param cache to query for cached items
     */
    protected Repository(@NonNull ICache cache) {
        mCache = cache;
        mCallCollapser = new BaseCallCollapser();
        mLastRefreshMoment = System.currentTimeMillis();
    }

    /**
     * Constructs a basic repository with a custom {@link ICallCollapser}
     *
     * @param cache         to query for cached items
     * @param callCollapser a custom {@link ICallCollapser} to handle http requests
     */
    protected Repository(@NonNull ICache cache, @NonNull ICallCollapser callCollapser) {
        mCache = cache;
        mCallCollapser = callCollapser;
        mLastRefreshMoment = System.currentTimeMillis();
    }

    /**
     * Queries the corresponding environment, with no update, in order to retrieve information and
     * handle it to the user.
     *
     * @see #query(int, Class, Object, Call, IRepositoryCallback, IUpdate)
     */
    protected final <T> void query(@AccessPolicy int policy, @NonNull Class<T> clazz,
                                   @NonNull Object key, @NonNull Call<T> call,
                                   @NonNull IRepositoryCallback<T> callback) {
        query(policy, clazz, key, call, callback, null);
    }

    /**
     * Queries the corresponding environment in order to retrieve information and handle it to the
     * user.
     * <p/>
     * Whether it checks cache, does the request and consequently notifies error is determined by
     * {@link AccessPolicy}.
     *
     * @param policy   policy to use for the query
     * @param clazz    {@link Class<T>} of the object to query
     * @param key      to identify cache data
     * @param call     request that retrieves asked information
     * @param callback that notifies the result of the query
     * @param update   optional update to apply
     */
    protected final <T> void query(@AccessPolicy int policy, @NonNull Class<T> clazz,
                                   @NonNull Object key, @NonNull Call<T> call,
                                   @NonNull IRepositoryCallback<T> callback,
                                   @Nullable IUpdate<T> update) {
        if (accessCache(policy)) {
            T cachedData = update != null ? applyUpdate(clazz, key, update)
                    : mCache.read(clazz, key);

            if (cachedData != null) {
                if (shouldInvalidateCache(policy)) {
                    mCache.clear(clazz, key);
                } else {
                    callback.onSuccess(cachedData);
                    return;
                }
            } else if (policy == CACHE_ONLY) {
                callback.onError(CACHE_MISS_ERROR_CODE);
                return;
            }
        }

        fetchData(clazz, call, callback);
    }

    /**
     * @param policy policy to check
     * @return wether the policy indicates that the query should access the cache.
     */
    private boolean accessCache(@AccessPolicy int policy) {
        return policy != CACHE_NONE;
    }

    /**
     * Applies an update to the object in cache and updates the refresh timer.
     *
     * @param clazz  of the object
     * @param key    to identify the element
     * @param update to apply
     * @return the updated element.
     */
    private <T> T applyUpdate(@NonNull Class<T> clazz, @NonNull Object key,
                              @NonNull IUpdate<T> update) {
        updateRefreshMoment();
        return mCache.update(clazz, key, update);
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
                (System.currentTimeMillis() - mLastRefreshMoment) >= getRefreshDelta();
    }

    /**
     * Makes a request and notifies accordingly. In case of success, it updates the {@link Cache}.
     *
     * @param clazz    {@link Class<T>} of the information to query
     * @param call     request to be done
     * @param callback that notifies the result of the query
     * @throws IllegalStateException if the <code>call</code> is either executed or cancelled.
     */
    private <T> void fetchData(@NonNull final Class<T> clazz, @NonNull Call<T> call,
                               @NonNull final IRepositoryCallback<T> callback) {
        if (call.isExecuted() || call.isCanceled()) {
            throw new IllegalStateException("Call should be ready to use");
        }

        mCallCollapser.enqueue(call, new WoloxCallback<T>() {
            @Override
            public void onResponseSuccessful(T data) {
                mCache.save(clazz, data);
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
     * Time interval, expressed in milliseconds, in which the repository's data is considered
     * up-to-date by the {@link #TIME_RESOLVE} policy. If said interval passed between the last
     * information update from the network, the cache is invalidated and the request to API is
     * executed.
     * </p>
     * Ie: If this method returns 2000, then 2 seconds have to elapse before the data is considered
     * 'dirty' by the {@link #TIME_RESOLVE} policy.
     *
     * @return repository delta time provided for {@link #TIME_RESOLVE} queries.
     */
    @IntRange(from = 0)
    protected abstract long getRefreshDelta();

}
