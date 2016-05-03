package com.danilov.acentrifugo.message.presence;

import com.danilov.acentrifugo.message.DownstreamMessage;

import org.json.JSONObject;

/**
 * Created by semyon on 29.04.16.
 */
public class JoinMessage extends BaseJoinLeftMessage {

    public JoinMessage(final JSONObject jsonObject) {
        super(jsonObject);
    }

}