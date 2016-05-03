package com.danilov.acentrifugo.listener;

import com.danilov.acentrifugo.message.presence.PresenceMessage;

/**
 * Created by semyon on 29.04.16.
 */
public interface PresenceListener {

    void onPresence(final PresenceMessage presenceMessage);

}
