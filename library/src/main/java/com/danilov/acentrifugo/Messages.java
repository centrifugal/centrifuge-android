package com.danilov.acentrifugo;

import android.os.Message;

import com.danilov.acentrifugo.subscription.SubscriptionRequest;

import org.json.JSONObject;

/**
 * This file is part of ACentrifugo.
 *
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by semyon on 08.02.16.
 * */
class Messages {

    public static final int SUBSCRIBE_MESSAGE_ID = 0;
    public static final int NEW_WS_MESSAGE = 1;

    public static Message getSubscribeMessage(final String channel, final String channelToken) {
        Message message = Message.obtain();
        message.obj = new SubscriptionRequest(channel, channelToken);
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
