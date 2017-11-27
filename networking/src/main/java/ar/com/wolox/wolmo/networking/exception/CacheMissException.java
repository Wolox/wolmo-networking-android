package ar.com.wolox.wolmo.networking.exception;

import ar.com.wolox.wolmo.networking.offline.Repository;

/**
 * Raised whenever a {@link Repository#query} misses cache when it shouldn't.
 */
public final class CacheMissException extends RuntimeException {

    private static final String UNEXPECTED_CACHE_MISS_MESSAGE = "There was an unexpected cache miss";

    public CacheMissException() {
        super(UNEXPECTED_CACHE_MISS_MESSAGE);
    }

}
