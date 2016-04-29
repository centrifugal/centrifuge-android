package com.danilov.acentrifugo.message;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class PresenceMessage extends DownstreamMessage {

    public PresenceMessage(final JSONObject jsonObject) {
        super(jsonObject);
    }

}