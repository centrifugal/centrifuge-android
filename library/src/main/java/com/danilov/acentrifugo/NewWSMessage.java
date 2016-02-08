package com.danilov.acentrifugo;

import org.json.JSONObject;

/**
 * Created by semyon on 08.02.16.
 */
class NewWSMessage {

    JSONObject message;

    public NewWSMessage(final JSONObject message) {
        this.message = message;
    }
}