package ar.com.wolox.wolmo.networking.offline;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Checks whether the refresh delta time has passed.
 * If it has, it refreshes cache by getting data from API. Else, it returns cache data.
 * </p>
 * For example, if the field returns 2000, then 2 seconds have to elapse before the data
 * is considered 'dirty'.
 */
// TODO: Comment
public abstract class TimeResolveQueryStrategy<T, C> implements Repository.QueryStrategy<T, C> {

    /**
     * Default refresh delta time. Users can modify it to set the default refresh delta time for
     * future created instances of {@link Repository}.
     */
    public static long DEFAULT_REFRESH_DELTA_TIME = TimeUnit.HOURS.toMillis(1);

    private long mLastRefreshMoment = System.currentTimeMillis();

    private final long mRefreshDeltaInMillis;

    public TimeResolveQueryStrategy(@IntRange(from = 1) long refreshDeltaInMillis) {
        mRefreshDeltaInMillis = refreshDeltaInMillis;
    }

    @Nullable
    @Override
    public T readLocalSource(@NonNull C cache) {
        if (shouldInvalidateCache()) {
            invalidate(cache);
            return null;
        }

        return read2(cache);
    }

    public abstract void invalidate(@NonNull C cache);

    @Override
    public void consumeRemoteSource(@NonNull T data, @NonNull C cache) {
        updateRefreshMoment();

        refresh(data, cache);
    }

    // TODO: Rename
    public abstract T read2(@NonNull C cache);

    public abstract void refresh(@NonNull T data, @NonNull C cache);

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
    private boolean shouldInvalidateCache() {
        return (System.currentTimeMillis() - mLastRefreshMoment) >= mRefreshDeltaInMillis;
    }

}
