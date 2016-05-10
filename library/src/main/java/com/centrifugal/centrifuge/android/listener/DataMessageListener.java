package com.centrifugal.centrifuge.android.listener;

import com.centrifugal.centrifuge.android.message.DataMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface DataMessageListener {

    void onNewDataMessage(final DataMessage message);

}
