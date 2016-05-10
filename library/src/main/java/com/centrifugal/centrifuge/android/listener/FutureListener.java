package com.centrifugal.centrifuge.android.listener;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface FutureListener<T> {

    void onData(final T data);

}
