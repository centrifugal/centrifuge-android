package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class DownstreamMessage {

    protected String UUID;

    protected JSONObject body;

    protected JSONObject originalMessage;

    public DownstreamMessage(final JSONObject jsonObject) {
        this.originalMessage = jsonObject;
        UUID = jsonObject.optString("uid");
        body = jsonObject.optJSONObject("body");
    }

    public String getUUID() {
        return UUID;
    }

    public JSONObject getBody() {
        return body;
    }

    public JSONObject getOriginalMessage() {
        return originalMessage;
    }

}