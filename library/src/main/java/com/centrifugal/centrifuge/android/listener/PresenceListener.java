package com.centrifugal.centrifuge.android.listener;

import com.centrifugal.centrifuge.android.message.presence.PresenceMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface PresenceListener {

    void onPresence(final PresenceMessage presenceMessage);

}
