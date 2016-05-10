package com.centrifugal.centrifuge.android.message.presence;

import org.json.JSONObject;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public class JoinMessage extends BaseJoinLeftMessage {

    public JoinMessage(final JSONObject jsonObject) {
        super(jsonObject);
    }

}