package com.danilov.acentrifugo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by semyon on 08.02.16.
 */
class Subscription {

    @NonNull
    private String channel;

    @Nullable
    private String channelToken;

    @NonNull
    private String info = "";

    public Subscription(@NonNull final String channel) {
        this.channel = channel;
    }

    public Subscription(@NonNull final String channel, @Nullable final String channelToken) {
        this.channel = channel;
        this.channelToken = channelToken;
    }

    public Subscription(@NonNull final String channel, @Nullable final String channelToken, @NonNull final String info) {
        this.channel = channel;
        this.channelToken = channelToken;
        this.info = info;
    }

    @NonNull
    public String getChannel() {
        return channel;
    }

    public void setChannel(@NonNull final String channel) {
        this.channel = channel;
    }

    @Nullable
    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(@Nullable final String channelToken) {
        this.channelToken = channelToken;
    }

    @NonNull
    public String getInfo() {
        return info;
    }

    public void setInfo(@NonNull final String info) {
        this.info = info;
    }

}
