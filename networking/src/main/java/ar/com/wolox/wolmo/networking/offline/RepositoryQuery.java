package ar.com.wolox.wolmo.networking.offline;

import android.support.annotation.NonNull;

import ar.com.wolox.wolmo.networking.utils.Consumer;

// TODO: Comment
public abstract class RepositoryQuery<T> implements Runnable {

    private Consumer<T> successConsumer;
    private Consumer<Throwable> errorConsumer;

    public RepositoryQuery<T> onSuccess(@NonNull Consumer<T> successConsumer) {
        this.successConsumer = successConsumer;
        return this;
    }

    public RepositoryQuery<T> onError(@NonNull Consumer<Throwable> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }

    void doOnSuccess(T data) {
        if (successConsumer != null) successConsumer.accept(data);
    }

    void doOnError(Throwable throwable) {
        if (errorConsumer != null) errorConsumer.accept(throwable);
    }

}
