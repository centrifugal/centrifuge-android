package com.centrifugal.centrifuge.android.subscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 04.05.2016.
 */
//TODO: decide if we need to keep channel's users list
public class ActiveSubscription {

    @Nonnull
    private SubscriptionRequest initialRequest;

    @Nullable
    private String lastMessageId;

    private boolean connected = false;

    public ActiveSubscription(@Nonnull final SubscriptionRequest initialRequest) {
        this.initialRequest = initialRequest;
    }

    public void updateLastMessage(@Nonnull final String newLastMessageId) {
        this.lastMessageId = newLastMessageId;
    }

    @Nonnull
    public SubscriptionRequest getInitialRequest() {
        return initialRequest;
    }

    @Nullable
    public String getLastMessageId() {
        return lastMessageId;
    }

    @Nonnull
    public String getChannel() {
        return initialRequest.getChannel();
    }

    @Nonnull
    public String getInfo() {
        return initialRequest.getInfo();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(final boolean connected) {
        this.connected = connected;
    }

}