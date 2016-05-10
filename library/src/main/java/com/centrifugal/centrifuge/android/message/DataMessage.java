package com.centrifugal.centrifuge.android.message;

import com.centrifugal.centrifuge.android.credentials.Info;
import com.centrifugal.centrifuge.android.credentials.User;

import org.json.JSONObject;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 01.05.2016.
 * */
public class DataMessage extends DownstreamMessage {

    private String data;

    @Nonnull
    private String UUID;

    @Nonnull
    private String channel;

    @Nullable
    private Info info;

    @Nonnull
    private Date timestamp;

    public static DataMessage fromBody(final JSONObject jsonObject) {
        DataMessage dataMessage = new DataMessage();
        dataMessage.setBody(jsonObject);
        dataMessage.setOriginalMessage(jsonObject);
        dataMessage.init(jsonObject);
        return dataMessage;
    }

    public DataMessage() {
    }

    public DataMessage(final JSONObject jsonObject) {
        super(jsonObject);
        init(body);
    }

    private void init(@Nonnull final JSONObject body) {
        Object dataObj = body.opt("data");
        if (dataObj != null) {
            data = dataObj.toString();
        }

        UUID = body.optString("uid");
        channel = body.optString("channel");
        @Nullable JSONObject infoJSON = body.optJSONObject("info");

        if (infoJSON != null) {
            String userString = infoJSON.optString("user");
            String clientString = infoJSON.optString("client");
            User user = new User(userString, clientString);
            JSONObject defaultInfo = infoJSON.optJSONObject("default_info");
            JSONObject channelInfo = infoJSON.optJSONObject("channel_info");
            info = new Info(user, defaultInfo, channelInfo);
        }


        String timestampString = body.optString("timestamp");
        timestamp = new Date(Long.valueOf(timestampString));
    }

    public String getData() {
        return data;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    @Nullable
    public Info getInfo() {
        return info;
    }

    @Nonnull
    public Date getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public String getUUID() {
        return UUID;
    }

}