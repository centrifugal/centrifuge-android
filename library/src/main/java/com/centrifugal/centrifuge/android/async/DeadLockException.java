package com.centrifugal.centrifuge.android.async;

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
 * Created by Semyon on 02.05.2016.
 * */
public class DeadLockException extends RuntimeException {

    public DeadLockException() {
    }

    public DeadLockException(final String detailMessage) {
        super(detailMessage);
    }

    public DeadLockException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeadLockException(final Throwable throwable) {
        super(throwable);
    }

}