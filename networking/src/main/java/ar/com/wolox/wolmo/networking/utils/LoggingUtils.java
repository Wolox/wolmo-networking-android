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

import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Utils to create {@link HttpLoggingInterceptor} for {@link OkHttpClient}.
 */
public class LoggingUtils {

    private LoggingUtils() {}

    /**
     * Returns a {@link HttpLoggingInterceptor} with a default level of {@link
     * HttpLoggingInterceptor.Level#BODY}.
     *
     * @return New instance of interceptor
     */
    public static HttpLoggingInterceptor buildHttpLoggingInterceptor() {
        return buildHttpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
    }

    /**
     * Returns a {@link HttpLoggingInterceptor} with the level given by <b>level</b>.
     *
     * @param level - Logging level for the interceptor.
     * @return New instance of interceptor
     */
    public static HttpLoggingInterceptor buildHttpLoggingInterceptor(
          @NonNull HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(level);
        return loggerInterceptor;
    }
}
