package com.danilov.acentrifugo.async;

import android.support.annotation.Nullable;

import com.danilov.acentrifugo.listener.FutureListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by semyon on 29.04.16.
 */
public class Future<T> {

    @Nullable
    private FutureListener<T> futureListener;

    private volatile T data;

    private Thread restrictedThread;

    @Nullable
    public T blockingGet(final long timeout, @Nullable final TimeUnit timeUnit) {
        if (Thread.currentThread() == restrictedThread) {
            throw new RuntimeException("You are trying to block the thread, which will " +
                    "process request. This results to a deadlock.");
        }
        if (data == null) {
            synchronized (this) {
                while (data == null) {
                    try {
                        if (timeout == -1) {
                            this.wait();
                        } else {
                            TimeUnit tm = timeUnit == null ? TimeUnit.MILLISECONDS : timeUnit;
                            long millis = tm.toMillis(timeout);
                            this.wait(millis);
                            //FIXME: really dirty, timeout is not guaranteed at all
                            break;
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }
        return data;
    }

    @Nullable
    public T blockingGet() {
        return blockingGet(-1, null);
    }

    public void setRestrictedThread(final Thread thread) {
        this.restrictedThread = thread;
    }

    public void then(final FutureListener<T> futureListener) {
        this.futureListener = futureListener;
    }

    public synchronized void setData(final T data) {
        this.data = data;
        if (futureListener != null) {
            futureListener.onData(data);
        }
        this.notifyAll();
    }

}
