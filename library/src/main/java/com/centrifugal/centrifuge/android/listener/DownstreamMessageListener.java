package com.centrifugal.centrifuge.android.listener;

import com.centrifugal.centrifuge.android.message.DownstreamMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface DownstreamMessageListener {

    void onDownstreamMessage(final DownstreamMessage message);

}
