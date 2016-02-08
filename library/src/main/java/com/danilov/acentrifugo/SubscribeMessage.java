package com.danilov.acentrifugo;

/**
 * Created by semyon on 08.02.16.
 */
class SubscribeMessage {

    String channel;
    String channelToken;

    public SubscribeMessage(final String channel, final String channelToken) {
        this.channel = channel;
        this.channelToken = channelToken;
    }

}
