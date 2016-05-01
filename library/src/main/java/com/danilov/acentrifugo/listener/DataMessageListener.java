package com.danilov.acentrifugo.listener;

import com.danilov.acentrifugo.message.DataMessage;

/**
 * Created by Semyon on 01.05.2016.
 */
public interface DataMessageListener {

    void onNewDataMessage(final DataMessage message);

}
