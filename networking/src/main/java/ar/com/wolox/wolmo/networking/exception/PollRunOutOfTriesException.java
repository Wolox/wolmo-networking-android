package ar.com.wolox.wolmo.networking.exception;

import android.support.annotation.NonNull;

import retrofit2.Call;

/**
 * {@link Exception} raised when polling an endpoint, for a maximum amount of tries, consumes all
 * of its tries available (ie: reaches 0).
 */
public final class PollRunOutOfTriesException extends RuntimeException {

    public PollRunOutOfTriesException(@NonNull Call call) {
        super("Polling to " + call.request().url().toString() + " failed after running out of tries");
    }

}
