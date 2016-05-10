package com.centrifugal.centrifuge.android.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 30.04.2016.
 * */
public class DataLock<T> {

    private CountDownLatch internalLock = new CountDownLatch(1);

    private T data = null;

    public T lockAndGet() {
        return lockAndGet(-1, null);
    }

    public T lockAndGet(final long timeout, final TimeUnit unit) {
        if (checkData()) {
            return data;
        }
        while (true) {
            if (timeout == -1) {
                try {
                    internalLock.await();
                } catch (InterruptedException e) {}
            } else {
                try {
                    internalLock.await(timeout, unit);
                } catch (InterruptedException e) {
                    break;
                }
                return data;
            }
            if (checkData() || internalLock.getCount() == 0) {
                return data;
            }
        }
        return data;
    }

    public void setData(final T data) {
        this.data = data;
        internalLock.countDown();
    }

    private synchronized boolean checkData() {
        return data != null;
    }

}