package com.danilov.acentrifugo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by semyon on 08.02.16.
 */
public class Subscription {

    @Nonnull
    private String channel;

    @Nullable
    private String channelToken;

    @Nonnull
    private String info = "";

    public Subscription(@Nonnull final String channel) {
        this.channel = channel;
    }

    public Subscription(@Nonnull final String channel, @Nullable final String channelToken) {
        this.channel = channel;
        this.channelToken = channelToken;
    }

    public Subscription(@Nonnull final String channel, @Nullable final String channelToken, @Nonnull final String info) {
        this.channel = channel;
        this.channelToken = channelToken;
        this.info = info;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    public void setChannel(@Nonnull final String channel) {
        this.channel = channel;
    }

    @Nullable
    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(@Nullable final String channelToken) {
        this.channelToken = channelToken;
    }

    @Nonnull
    public String getInfo() {
        return info;
    }

    public void setInfo(@Nonnull final String info) {
        this.info = info;
    }

}
