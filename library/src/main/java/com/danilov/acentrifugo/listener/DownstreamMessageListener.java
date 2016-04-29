package com.danilov.acentrifugo.listener;

import com.danilov.acentrifugo.message.DownstreamMessage;

/**
 * Created by semyon on 29.04.16.
 */
public interface DownstreamMessageListener {

    void onDownstreamMessage(final DownstreamMessage message);

}
