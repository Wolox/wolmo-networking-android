package ar.com.wolox.wolmo.networking.utils;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.android.internal.util.Predicate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import ar.com.wolox.wolmo.networking.exception.PollRunOutOfTriesException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Common uses of Retrofit's {@link Call}, {@link Callback} and their interactions.
 */
public class CallUtils {

    private CallUtils() {}

    /**
     * Polls with a delay in-between calls.
     *
     * @param tries amount of tries
     * @param call to poll
     * @param pollingCondition that dictates whether to keep polling
     * @param callback to be notified when polling ends
     * @param delay to apply in-between calls
     * @param timeoutUnit to convert delay

     * @return a {@link Timer} which is controlling the polling
     */
    public static <T> Timer pollWithDelay(@IntRange(from = 1) final int tries,
                                          @NonNull final Call<T> call,
                                          @NonNull final Predicate<Response<T>> pollingCondition,
                                          @NonNull final Callback<T> callback,
                                          @IntRange(from = 0) long delay,
                                          @NonNull TimeUnit timeoutUnit) {
        Timer pollingTimer = new Timer();
        pollWithDelay(tries, call, pollingCondition, callback,
                timeoutUnit.toMillis(delay), pollingTimer);
        return pollingTimer;
    }

    private static <T> void pollWithDelay(@IntRange(from = 1) final int triesRemaining,
                                           @NonNull final Call<T> call,
                                           @NonNull final Predicate<Response<T>> pollingCondition,
                                           @NonNull final Callback<T> callback,
                                           @IntRange(from = 0) long delayInMillis,
                                           @NonNull final Timer pollingTimer) {
        if (triesRemaining <= 0) callback.onFailure(call, new PollRunOutOfTriesException(call));

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!pollingCondition.apply(response)) {
                    callback.onResponse(call, response);
                    return;
                }

                pollingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pollWithDelay(triesRemaining - 1, call.clone(),
                                pollingCondition, callback, delayInMillis, pollingTimer);
                    }
                }, delayInMillis);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

}
