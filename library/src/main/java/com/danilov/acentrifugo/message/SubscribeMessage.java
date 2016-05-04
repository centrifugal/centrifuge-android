package com.danilov.acentrifugo.message;

import com.danilov.acentrifugo.message.DownstreamMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import javax.annotation.Nullable;

/**
 * This file is part of ACentrifugo.
 * <p/>
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by Semyon on 04.05.2016.
 */
public class SubscribeMessage extends DownstreamMessage {

    @Nullable
    private String channel;

    @Nullable
    private Boolean status;

    @Nullable
    private JSONArray recoveredMessages;

    public SubscribeMessage(final JSONObject jsonObject) {
        super(jsonObject);
        channel = body.optString("channel");
        if (body.has("status")) {
            status = body.optBoolean("status");
        }
        recoveredMessages = (JSONArray) body.optJSONArray("messages");
    }

    @Nullable
    public String getChannel() {
        return channel;
    }

    @Nullable
    public Boolean getStatus() {
        return status;
    }

    @Nullable
    public JSONArray getRecoveredMessages() {
        return recoveredMessages;
    }

}