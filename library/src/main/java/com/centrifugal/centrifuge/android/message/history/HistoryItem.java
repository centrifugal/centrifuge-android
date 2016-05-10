package com.centrifugal.centrifuge.android.message.history;

import org.json.JSONObject;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 04.05.2016.
 */
public class HistoryItem {

    @Nonnull
    private String UUID;

    @Nonnull
    private Date timestamp;

    @Nonnull
    private String channel;

    @Nullable
    private JSONObject data;

    public HistoryItem(@Nonnull final JSONObject messageJSON) {
        data = messageJSON.optJSONObject("data");
        UUID = messageJSON.optString("uid");
        channel = messageJSON.optString("channel");
        String timestampString = messageJSON.optString("timestamp");
        timestamp = new Date(Long.valueOf(timestampString));
    }

    @Nonnull
    public String getUUID() {
        return UUID;
    }

    @Nonnull
    public Date getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

}