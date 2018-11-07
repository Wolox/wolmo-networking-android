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
package ar.com.wolox.wolmo.networking.utils;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import ar.com.wolox.wolmo.core.java8.Predicate;
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
     *
     * @return a {@link Timer} which is controlling the polling
     */
    public static <T> Timer pollWithDelay(@IntRange(from = 1) final int tries,
                                          @NonNull final Call<T> call,
                                          @NonNull final Predicate<Response<T>> pollingCondition,
                                          @NonNull final Callback<T> callback,
                                          @IntRange(from = 0) long delay,
                                          @NonNull TimeUnit timeoutUnit) {
        // Timer can be used safely since Retrofit callback is called on UiThread
        Timer pollingTimer = new Timer();
        pollWithDelay(tries, call, pollingCondition, callback, timeoutUnit.toMillis(delay),
                pollingTimer);
        return pollingTimer;
    }

    private static <T> void pollWithDelay(@IntRange(from = 1) final int triesRemaining,
                                          @NonNull final Call<T> call,
                                          @NonNull final Predicate<Response<T>> pollingCondition,
                                          @NonNull final Callback<T> callback,
                                          @IntRange(from = 0) long delayInMillis,
                                          @NonNull final Timer pollingTimer) {

        if (triesRemaining <= 0) {
            callback.onFailure(call, new PollRunOutOfTriesException(call));
            return;
        }

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (!pollingCondition.test(response)) {
                    callback.onResponse(call, response);
                    return;
                }

                pollingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pollWithDelay(triesRemaining - 1, call.clone(), pollingCondition, callback,
                                delayInMillis, pollingTimer);
                    }
                }, delayInMillis);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
}
