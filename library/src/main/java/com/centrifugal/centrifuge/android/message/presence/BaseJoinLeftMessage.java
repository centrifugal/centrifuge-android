package com.centrifugal.centrifuge.android.message.presence;


import com.centrifugal.centrifuge.android.message.DownstreamMessage;

import org.json.JSONObject;

import javax.annotation.Nullable;

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
 * Created by Semyon on 03.05.2016.
 * */
public class BaseJoinLeftMessage extends DownstreamMessage {

    @Nullable
    private String channel;

    @Nullable
    private String user;

    @Nullable
    private String client;

    public BaseJoinLeftMessage(final JSONObject jsonObject) {
        super(jsonObject);
        channel = body.optString("channel");
        @Nullable JSONObject data = body.optJSONObject("data");
        if (data != null) {
            user = data.optString("user");
            client = data.optString("client");
        }
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    @Nullable
    public String getUser() {
        return user;
    }

    @Nullable
    public String getClient() {
        return client;
    }

}