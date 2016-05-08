package com.centrifugal.centrifuge.android.subscription;

import javax.annotation.Nonnull;
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
 * Created by semyon on 08.02.16.
 * */
public class SubscriptionRequest {

    @Nonnull
    private String channel;

    @Nullable
    private String channelToken;

    @Nonnull
    private String info = "";

    public SubscriptionRequest(@Nonnull final String channel) {
        this.channel = channel;
    }

    public SubscriptionRequest(@Nonnull final String channel, @Nullable final String channelToken) {
        this.channel = channel;
        this.channelToken = channelToken;
    }

    public SubscriptionRequest(@Nonnull final String channel, @Nullable final String channelToken, @Nonnull final String info) {
        this.channel = channel;
        this.channelToken = channelToken;
        this.info = info;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    public void setChannel(@Nonnull final String channel) {
        this.channel = channel;
    }

    @Nullable
    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(@Nullable final String channelToken) {
        this.channelToken = channelToken;
    }

    @Nonnull
    public String getInfo() {
        return info;
    }

    public void setInfo(@Nonnull final String info) {
        this.info = info;
    }

}
