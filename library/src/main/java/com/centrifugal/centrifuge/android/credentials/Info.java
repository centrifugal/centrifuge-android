package com.centrifugal.centrifuge.android.credentials;

import javax.annotation.Nonnull;

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
public class Info {

    @Nonnull
    private User user;

    @Nullable
    private JSONObject defaultInfo;

    @Nullable
    private JSONObject channelInfo;

    public Info(@Nonnull final User user, @Nullable final JSONObject defaultInfo, @Nullable final JSONObject channelInfo) {
        this.user = user;
        this.defaultInfo = defaultInfo;
        this.channelInfo = channelInfo;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nullable
    public JSONObject getDefaultInfo() {
        return defaultInfo;
    }

    @Nullable
    public JSONObject getChannelInfo() {
        return channelInfo;
    }

}