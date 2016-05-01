package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by Semyon on 01.05.2016.
 */
public class DataMessage extends DownstreamMessage {

    private JSONObject data;

    public DataMessage(final JSONObject jsonObject) {
        super(jsonObject);
        data = getBody().optJSONObject("data");
    }

    public JSONObject getData() {
        return data;
    }
}
