package com.danilov.acentrifugo;

import android.os.Message;

import org.json.JSONObject;

/**
 * Created by semyon on 08.02.16.
 */
class Messages {

    public static final int SUBSCRIBE_MESSAGE_ID = 0;
    public static final int NEW_WS_MESSAGE = 1;

    public static Message getSubscribeMessage(final String channel, final String channelToken) {
        Message message = Message.obtain();
        message.obj = new Subscription(channel, channelToken);
        message.what = SUBSCRIBE_MESSAGE_ID;
        return message;
    }

    public static Message getNewWSMessage(final JSONObject msg) {
        Message message = Message.obtain();
        message.obj = new NewWSMessage(msg);
        message.what = NEW_WS_MESSAGE;
        return message;
    }

}
