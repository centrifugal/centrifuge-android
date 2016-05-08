package com.centrifugal.centrifuge.android.message.history;

import com.centrifugal.centrifuge.android.message.DownstreamMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of ACentrifugo.
 *
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
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