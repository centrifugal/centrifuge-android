package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class JoinMessage extends DownstreamMessage {

    public JoinMessage(final JSONObject jsonObject) {
        super(jsonObject);
    }

}