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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Checks whether the refresh delta time has passed.
 * If it has, it refreshes cache by getting data from API. Else, it returns cache data.
 * </p>
 * For example, if it was initialized with 2000, then 2 seconds have to elapse before the data
 * is considered 'dirty'.
 */
public abstract class TimeResolveQueryStrategy<T, C> implements Repository.QueryStrategy<T, C> {

    /**
     * Default refresh delta time. Users can modify it to set the default refresh delta time for
     * future created instances of {@link TimeResolveQueryStrategy}.
     */
    public static long DEFAULT_REFRESH_DELTA_TIME = TimeUnit.HOURS.toMillis(1);

    private long mLastRefreshMoment = System.currentTimeMillis();
    private final long mRefreshDeltaInMillis;

    /**
     * Creates an instance of the class with a refresh delta time.
     *
     * @param refreshDeltaInMillis to use
     */
    public TimeResolveQueryStrategy(@IntRange(from = 1) long refreshDeltaInMillis) {
        mRefreshDeltaInMillis = refreshDeltaInMillis;
    }

    /**
     * Creates an instance of the class with the {@link #DEFAULT_REFRESH_DELTA_TIME}.
     */
    public TimeResolveQueryStrategy() {
        this(DEFAULT_REFRESH_DELTA_TIME);
    }

    @Nullable
    @Override
    public final T readLocalSource(@NonNull C cache) {
        if (shouldInvalidateCache()) {
            invalidate(cache);
            return null;
        }

        return cleanReadLocalSource(cache);
    }

    /**
     * Called when enough time has passed to consider local source 'dirty' and take actions over the
     * {@link C cache} to reflect those changes.
     *
     * @param cache to interact with
     */
    public abstract void invalidate(@NonNull C cache);

    /**
     * Called when the local source is read when considered 'clean'.
     *
     * @param cache to read from
     *
     * @return the {@link T} data read from cache
     * @see #readLocalSource(C)
     */
    public abstract T cleanReadLocalSource(@NonNull C cache);

    @Override
    public final void consumeRemoteSource(@NonNull T data, @NonNull C cache) {
        updateRefreshMoment();
        refresh(data, cache);
    }

    /**
     * Called when local source is considered 'dirty' and data should be refreshed with remote
     * source data.
     *
     * @param data retrieved from remote source
     * @param cache to interact with
     */
    public abstract void refresh(@NonNull T data, @NonNull C cache);

    /**
     * Sets {@link #mLastRefreshMoment} to the current moment in time.
     */
    private void updateRefreshMoment() {
        mLastRefreshMoment = System.currentTimeMillis();
    }

    /**
     * @return whether cached data should be invalidated regarding the last time it was refreshed.
     */
    private boolean shouldInvalidateCache() {
        return (System.currentTimeMillis() - mLastRefreshMoment) >= mRefreshDeltaInMillis;
    }
}
