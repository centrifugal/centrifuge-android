package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class DownstreamMessage {

    private String UUID;

    private JSONObject body;

    public DownstreamMessage(final JSONObject jsonObject) {
        UUID = jsonObject.optString("uid");
        body = jsonObject.optJSONObject("body");
    }

    public String getUUID() {
        return UUID;
    }

    public JSONObject getBody() {
        return body;
    }

}