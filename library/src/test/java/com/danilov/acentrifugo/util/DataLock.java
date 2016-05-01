package com.danilov.acentrifugo.util;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Semyon on 30.04.2016.
 */
public class DataLock<T> {

    private CountDownLatch internalLock = new CountDownLatch(1);

    private T data = null;

    public T lockAndGet() throws InterruptedException {
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
            if (checkData()) {
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