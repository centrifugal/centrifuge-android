package com.centrifugal.centrifuge.android.util;

import java.util.concurrent.CountDownLatch;
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
 * along with ACentrifugo.  If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
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