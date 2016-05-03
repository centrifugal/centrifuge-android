package com.danilov.acentrifugo.message;

import com.danilov.acentrifugo.credentials.Info;
import com.danilov.acentrifugo.credentials.User;

import org.json.JSONObject;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Semyon on 01.05.2016.
 */
public class DataMessage extends DownstreamMessage {

    private JSONObject data;

    @Nonnull
    private String channel;

    @Nonnull
    private Info info;

    @Nonnull
    private Date timestamp;

    public DataMessage(final JSONObject jsonObject) {
        super(jsonObject);
        data = body.optJSONObject("data");
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

    public JSONObject getData() {
        return data;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    @Nonnull
    public Info getInfo() {
        return info;
    }

    @Nonnull
    public Date getTimestamp() {
        return timestamp;
    }

}