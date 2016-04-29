package com.danilov.acentrifugo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by semyon on 08.02.16.
 */
class SubscribeMessage {

    @NonNull
    String channel;

    @NonNull
    String channelToken;

    @NonNull
    String info = "";

    public SubscribeMessage(@NonNull final String channel, @NonNull final String channelToken) {
        this.channel = channel;
        this.channelToken = channelToken;
    }

    public SubscribeMessage(@NonNull final String channel, @NonNull final String channelToken, @NonNull final String info) {
        this.channel = channel;
        this.channelToken = channelToken;
        this.info = info;
    }

}
