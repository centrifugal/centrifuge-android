package com.centrifugal.centrifuge.android.message;

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
 * Created by semyon on 29.04.16.
 * */
public class DownstreamMessage {

    protected String requestUUID;

    protected JSONObject body;

    protected JSONObject originalMessage;

    @Nullable
    private String error;

    public DownstreamMessage() {
    }

    public DownstreamMessage(final JSONObject jsonObject) {
        this.originalMessage = jsonObject;
        requestUUID = jsonObject.optString("uid");
        body = jsonObject.optJSONObject("body");
        if (jsonObject.has("error")) {
            error = jsonObject.optString("error");
        }
    }

    public String getRequestUUID() {
        return requestUUID;
    }

    public JSONObject getBody() {
        return body;
    }

    public JSONObject getOriginalMessage() {
        return originalMessage;
    }

    @Nullable
    public String getError() {
        return error;
    }

    void setOriginalMessage(final JSONObject originalMessage) {
        this.originalMessage = originalMessage;
    }

    void setBody(final JSONObject body) {
        this.body = body;
    }

}