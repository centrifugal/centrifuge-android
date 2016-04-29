package com.danilov.acentrifugo.listener;

/**
 * Created by semyon on 29.04.16.
 */
public interface FutureListener<T> {

    void onData(final T data);

}
