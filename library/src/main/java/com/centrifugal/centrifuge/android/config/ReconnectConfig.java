package com.centrifugal.centrifuge.android.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnegative;

/**
 * Created by semyon on 05.05.16.
 */
public class ReconnectConfig {

    private int maxReconnectCount;

    private long reconnectDelay;

    private int curReconnectCount = 0;

    public ReconnectConfig(final int maxReconnectCount, @Nonnegative final long reconnectDelay, final TimeUnit timeUnit) {
        this.maxReconnectCount = maxReconnectCount;
        this.reconnectDelay = timeUnit.toMillis(reconnectDelay);
    }

    public int getMaxReconnectCount() {
        return maxReconnectCount;
    }

    public void setMaxReconnectCount(final int maxReconnectCount) {
        this.maxReconnectCount = maxReconnectCount;
    }

    public long getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(final long reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public void incReconnectCount() {
        curReconnectCount++;
    }

    public boolean shouldReconnect() {
        return curReconnectCount < maxReconnectCount;
    }

}