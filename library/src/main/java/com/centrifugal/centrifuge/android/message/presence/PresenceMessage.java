package com.centrifugal.centrifuge.android.message.presence;

import javax.annotation.Nonnull;

import com.centrifugal.centrifuge.android.credentials.User;
import com.centrifugal.centrifuge.android.message.DownstreamMessage;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
public class PresenceMessage extends DownstreamMessage {

    @Nonnull
    private List<User> userList = new LinkedList<>();

    public PresenceMessage(final JSONObject jsonObject) {
        super(jsonObject);
        @Nullable JSONObject data = body.optJSONObject("data");
        if (data != null) {
            Iterator<String> iter = data.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                @Nullable JSONObject userJson = data.optJSONObject(key);
                if (userJson != null) {
                    String userString = userJson.optString("user");
                    String clientString = userJson.optString("client");
                    User user = new User(userString, clientString);
                    userList.add(user);
                }
            }
        }
    }

    @Nonnull
    public List<User> getUserList() {
        return userList;
    }

}