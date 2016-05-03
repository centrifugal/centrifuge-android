package com.danilov.acentrifugo.message.presence;

import javax.annotation.Nonnull;

import com.danilov.acentrifugo.credentials.User;
import com.danilov.acentrifugo.message.DownstreamMessage;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by semyon on 29.04.16.
 */
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