package ar.com.wolox.wolmo.networking.exception;

// TODO: Comment
public final class CacheMissException extends RuntimeException {

    private static final String UNEXPECTED_CACHE_MISS_MESSAGE = "There was an unexpected cache miss";

    public CacheMissException() {
        super(UNEXPECTED_CACHE_MISS_MESSAGE);
    }

}
