package com.danilov.acentrifugo.listener;

/**
 * Created by semyon on 29.04.16.
 */
public interface ConnectionListener {

    void onConnected();

    void onDisconnected(final int code, final String reason, final boolean remote);

}
