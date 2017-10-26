package ar.com.wolox.wolmo.networking.utils;

/**
 * Interface for consuming data of type {@link T}.
 */
public interface Consumer<T> {

    /**
     * @param data to consume
     */
    void accept(T data);

}
