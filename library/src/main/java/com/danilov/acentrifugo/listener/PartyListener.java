package com.danilov.acentrifugo.listener;

import com.danilov.acentrifugo.message.JoinMessage;
import com.danilov.acentrifugo.message.LeftMessage;

/**
 * Created by semyon on 29.04.16.
 */
public interface PartyListener {

    void onJoin(final JoinMessage joinMessage);

    void onLeave(final LeftMessage leftMessage);

}
