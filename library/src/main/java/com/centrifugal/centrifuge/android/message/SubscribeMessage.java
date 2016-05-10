package com.centrifugal.centrifuge.android.message;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 04.05.2016.
 */
public class SubscribeMessage extends DownstreamMessage {

    @Nullable
    private String channel;

    @Nullable
    private Boolean status;

    @Nullable
    private JSONArray recoveredMessages;

    public SubscribeMessage(final JSONObject jsonObject) {
        super(jsonObject);
        channel = body.optString("channel");
        if (body.has("status")) {
            status = body.optBoolean("status");
        }
        recoveredMessages = (JSONArray) body.optJSONArray("messages");
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    @Nullable
    public Boolean getStatus() {
        return status;
    }

    @Nullable
    public JSONArray getRecoveredMessages() {
        return recoveredMessages;
    }

}