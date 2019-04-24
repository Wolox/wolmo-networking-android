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
package ar.com.wolox.wolmo.networking.exception;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import ar.com.wolox.wolmo.networking.offline.Repository;

/**
 * Raised whenever a {@link Repository#query} goes to network and the response code is
 * not in the range [200..300).
 */
public final class NetworkResourceException extends Exception {

    private static final String REPORT_MESSAGE_FORMAT =
            "Network resource requested at %s yielded a %d error code";

    private final int mErrorCode;

    @SuppressLint("DefaultLocale")
    public NetworkResourceException(@NonNull String resourceUrl, int errorCode) {
        super(String.format(REPORT_MESSAGE_FORMAT, resourceUrl, errorCode));
        mErrorCode = errorCode;
    }

    /**
     * @return the code contained in the not successful response
     */
    public int getErrorCode() {
        return mErrorCode;
    }
}
