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

import retrofit2.Call;
import retrofit2.Callback;

/**
 * A Call Collapser is a mechanism of network optimization that prevents repetitive consecutive
 * HTTP requests that would most probably return the same response from the network.
 * <p>
 * It enqueues requests of the same type so only one HTTP request is sent over the network but
 * every local callback gets notified as if they have made the request.
 * <p>
 * Example:
 * Making several consecutive GET HTTP requests to "www.example.com/v1/users/$userID"
 * in a short interval will probably result in the same user data being retrieved. A Call Collapser
 * would instead make only one HTTP request and return the same result to every local caller.
 */
public interface ICallCollapser {

    /**
     * Handles the API call avoiding repetitive and useless requests as much as possible
     *
     * @param call to be made to the API
     * @param callback to be called after executing it
     */
    <T> void enqueue(@NonNull Call<T> call, @NonNull Callback<T> callback);
}
