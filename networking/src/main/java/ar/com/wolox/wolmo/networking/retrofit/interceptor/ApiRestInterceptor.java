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
package ar.com.wolox.wolmo.networking.retrofit.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An implementation of OkHTTP's {@link Interceptor} that adds common headers to every API
 * request and provides helper methods to add custom ones.
 */
public abstract class ApiRestInterceptor implements Interceptor {

    protected static final String CONTENT_TYPE_HEADER = "Content-Type";
    protected static final String ACCEPT_HEADER = "Accept";

    /**
     * Intercepts the API call and adds custom headers to the request. By default, it will
     * add both "Content-Type" and "Accept" headers.
     * If you wish to add more custom headers you may prefer using the method addHeaders() instead
     * of overwriting this one.
     *
     * @param chain an object provided by OkHTTP with data of the request being made
     *
     * @return an instance of {@link Response} by OkHTTP
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder =
                request.newBuilder().addHeader(CONTENT_TYPE_HEADER, "application/json")
                        .addHeader(ACCEPT_HEADER, "application/json");
        addHeaders(requestBuilder);
        request = requestBuilder.build();
        return chain.proceed(request);
    }

    /**
     * A helper method to add custom headers to the network request.
     *
     * @param requestBuilder an instance of {@link Request.Builder} that you can use to add custom
     * headers.
     */
    public abstract void addHeaders(@NonNull Request.Builder requestBuilder);
}
