package com.danilov.acentrifugo.credentials;

import javax.annotation.Nonnull;

import org.json.JSONObject;

import javax.annotation.Nullable;

/**
 * Created by Semyon on 03.05.2016.
 */
public class Info {

    @Nonnull
    private User user;

    @Nullable
    private JSONObject defaultInfo;

    @Nullable
    private JSONObject channelInfo;

    public Info(@Nonnull final User user, final JSONObject defaultInfo, final JSONObject channelInfo) {
        this.user = user;
        this.defaultInfo = defaultInfo;
        this.channelInfo = channelInfo;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nullable
    public JSONObject getDefaultInfo() {
        return defaultInfo;
    }

    @Nullable
    public JSONObject getChannelInfo() {
        return channelInfo;
    }

}