package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class LeftMessage extends DownstreamMessage {

    public LeftMessage(final JSONObject jsonObject) {
        super(jsonObject);
    }

}
