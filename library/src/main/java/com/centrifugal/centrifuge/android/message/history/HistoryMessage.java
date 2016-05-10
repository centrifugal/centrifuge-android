package com.centrifugal.centrifuge.android.message.history;

import com.centrifugal.centrifuge.android.message.DownstreamMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public class HistoryMessage extends DownstreamMessage {

    @Nonnull
    private List<HistoryItem> messages = new ArrayList<>();

    public HistoryMessage(final JSONObject jsonObject) {
        super(jsonObject);
        @Nullable JSONArray data = body.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject messageJSON = data.optJSONObject(i);
                HistoryItem dataMessage = new HistoryItem(messageJSON);
                messages.add(dataMessage);
            }
        }
    }

    @Nonnull
    public List<HistoryItem> getMessages() {
        return messages;
    }

}