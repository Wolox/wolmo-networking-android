package ar.com.wolox.wolmo.networking.exception;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import ar.com.wolox.wolmo.networking.offline.Repository;

/**
 * Raised whenever a {@link Repository#query} goes to network and the response code is
 * not in the range [200..300).
 */
public final class NetworkResourceException extends Exception {

    private static final String REPORT_MESSAGE_FORMAT = "Network resource requested at %s yielded a %d error code";

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
