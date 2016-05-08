package com.centrifugal.centrifuge.android.async;

import javax.annotation.Nullable;

import com.centrifugal.centrifuge.android.listener.FutureListener;

import java.util.concurrent.TimeUnit;

/**
 * This file is part of ACentrifugo.
 *
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by semyon on 29.04.16.
 * */
public class Future<T> {

    @Nullable
    private FutureListener<T> futureListener;

    private volatile T data;

    private Thread restrictedThread;

    @Nullable
    public T blockingGet(final long timeout, @Nullable final TimeUnit timeUnit) {
        if (Thread.currentThread() == restrictedThread) {
            throw new DeadLockException("You are trying to block the thread, which will " +
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
