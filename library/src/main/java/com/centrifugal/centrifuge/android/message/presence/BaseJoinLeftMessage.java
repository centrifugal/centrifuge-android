package com.centrifugal.centrifuge.android.message.presence;


import com.centrifugal.centrifuge.android.message.DownstreamMessage;

import org.json.JSONObject;

import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 03.05.2016.
 * */
public class BaseJoinLeftMessage extends DownstreamMessage {

    @Nullable
    private String channel;

    @Nullable
    private String user;

    @Nullable
    private String client;

    public BaseJoinLeftMessage(final JSONObject jsonObject) {
        super(jsonObject);
        channel = body.optString("channel");
        @Nullable JSONObject data = body.optJSONObject("data");
        if (data != null) {
            user = data.optString("user");
            client = data.optString("client");
        }
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    @Nullable
    public String getUser() {
        return user;
    }

    @Nullable
    public String getClient() {
        return client;
    }

}