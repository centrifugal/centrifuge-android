package com.danilov.acentrifugo.subscription;

import javax.annotation.Nonnull;
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
//TODO: decide if we need to keep channel's users list
public class ActiveSubscription {

    @Nonnull
    private SubscriptionRequest initialRequest;

    @Nullable
    private String lastMessageId;

    private boolean connected = false;

    public ActiveSubscription(@Nonnull final SubscriptionRequest initialRequest) {
        this.initialRequest = initialRequest;
    }

    public void updateLastMessage(@Nonnull final String newLastMessageId) {
        this.lastMessageId = newLastMessageId;
    }

    @Nonnull
    public SubscriptionRequest getInitialRequest() {
        return initialRequest;
    }

    @Nullable
    public String getLastMessageId() {
        return lastMessageId;
    }

    @Nonnull
    public String getChannel() {
        return initialRequest.getChannel();
    }

    @Nonnull
    public String getInfo() {
        return initialRequest.getInfo();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(final boolean connected) {
        this.connected = connected;
    }

}