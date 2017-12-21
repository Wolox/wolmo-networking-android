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
package ar.com.wolox.wolmo.networking.optimizations;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Thread-safe implementation of {@link ICallCollapser}.
 * Collapses GET requests into one by calling the first and reporting the result to every callback.
 */
public class BaseCallCollapser implements ICallCollapser {

    public static final String HTTP_METHOD_GET = "GET";

    private final ConcurrentHashMap<String, Queue<Callback>> mGetCallbackQueues;

    public BaseCallCollapser() {
        mGetCallbackQueues = new ConcurrentHashMap<>();
    }

    /**
     * Enqueues the call immediately if it's not a GET. This is done because only GET (read only)
     * operations can be collapsed, writing operations should't be altered.
     * <p>
     * Collapsing the call means adding it to the queue of the same
     * request, and executing it if it's the first to be added to said queue.
     *
     * @param call     to be enqueued
     * @param callback to be called when executing it
     */
    public final <T> void enqueue(@NonNull Call<T> call, @NonNull Callback<T> callback) {
        if (!isGetCall(call)) {
            call.enqueue(callback);
            return;
        }

        Queue<Callback> requestQueue = getQueueFromRequest(call);

        requestQueue.add(callback);
        if (requestQueue.size() > 1) return;
        collapsingEnqueue(call);
    }

    private boolean isGetCall(Call call) {
        return HTTP_METHOD_GET.equalsIgnoreCase(call.request().method());
    }

    /**
     * Retrieves the request URL of the {@link Call} and returns a queue for it. If it doesn't exist
     * yet, it creates it and adds it to {@link #mGetCallbackQueues}.
     *
     * @param call for the queue retrieval
     * @return a {@link Queue} associated to the given {@link Call} url.
     */
    @NonNull
    private Queue<Callback> getQueueFromRequest(Call call) {
        String url = call.request().url().toString();

        // Synchronization is needed here since it's safe per call but having multiple calls
        // makes it vulnerable.
        synchronized (mGetCallbackQueues) {
            if (mGetCallbackQueues.containsKey(url)) return mGetCallbackQueues.get(url);
        }

        LinkedList<Callback> requestQueue = new LinkedList<>();
        mGetCallbackQueues.put(url, requestQueue);
        return requestQueue;
    }

    /**
     * Calls {@link Call#enqueue(Callback)} with a {@link Callback<T>} that reports the result to
     * the request queue.
     *
     * @param call to execute
     */
    private <T> void collapsingEnqueue(@NonNull Call<T> call) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                applySuccessToQueue(call, response);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                applyFailureToQueue(call, t);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> void applySuccessToQueue(@NonNull Call<T> call, @NonNull Response<T> response) {
        Queue<Callback> requestQueue = getQueueFromRequest(call);

        Callback currentCallback;
        while ((currentCallback = requestQueue.poll()) != null) {
            currentCallback.onResponse(call, response);
        }

        removeQueueFromRequest(call);
    }

    @SuppressWarnings("unchecked")
    private <T> void applyFailureToQueue(@NonNull Call<T> call, @NonNull Throwable t) {
        Queue<Callback> requestQueue = getQueueFromRequest(call);

        Callback currentCallback;
        while ((currentCallback = requestQueue.poll()) != null) {
            currentCallback.onFailure(call, t);
        }

        removeQueueFromRequest(call);
    }

    private void removeQueueFromRequest(Call call) {
        mGetCallbackQueues.remove(call.request().url().toString());
    }

}
