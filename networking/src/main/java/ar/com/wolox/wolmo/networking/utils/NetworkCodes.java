/*
 * Copyright (c) Wolox S.A
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ar.com.wolox.wolmo.networking.utils;

/**
 * Common HTTP responses codes, useful when making API calls using Retrofit's
 * {@link retrofit2.Callback} or Wolmo's
 * {@link ar.com.wolox.wolmo.networking.retrofit.callback.NetworkCallback}
 */
public class NetworkCodes {

    private NetworkCodes() {}

    /**
     * 2XX Success
     */
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int OK_NO_CONTENT = 204;

    /**
     * 4XX Client errors
     */
    public static final int ERROR_BAD_REQUEST = 400;
    public static final int ERROR_UNAUTHORIZED = 401;
    public static final int ERROR_FORBIDDEN = 403;
    public static final int ERROR_NOT_FOUND = 404;
    public static final int ERROR_PRECONDITION_FAILED = 412;

    /**
     * 5XX Server errors
     */
    public static final int ERROR_INTERNAL = 500;

}