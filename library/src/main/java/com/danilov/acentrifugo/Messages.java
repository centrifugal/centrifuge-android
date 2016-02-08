package com.danilov.acentrifugo;

import android.os.Message;

/**
 * Created by semyon on 08.02.16.
 */
class Messages {

    public static final int SUBSCRIBE_MESSAGE_ID = 0;

    public static Message getSubscribeMessage(final String channel, final String channelToken) {
        Message message = Message.obtain();
        message.obj = new SubscribeMessage(channel, channelToken);
        message.what = SUBSCRIBE_MESSAGE_ID;
        return message;
    }

}
