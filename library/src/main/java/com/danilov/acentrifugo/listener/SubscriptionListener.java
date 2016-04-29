package com.danilov.acentrifugo.listener;

/**
 * Created by semyon on 29.04.16.
 */
public interface SubscriptionListener {

    void onSubscribed(final String channelName);

    void onUnsubscribed(final String channelName);

    void onSubscriptionError(final String channelName, final String error);

}
