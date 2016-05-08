package com.centrifugal.centrifuge.android;

import com.centrifugal.centrifuge.android.util.Signing;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by semyon on 29.04.16.
 * */
public class TestWebapp extends Dispatcher {


    @Override
    public MockResponse dispatch(final RecordedRequest request) throws InterruptedException {
        String timestamp = System.currentTimeMillis() + "";
        String path = request.getPath();
        String userId = "test-user-id";
//        String channelName = "test-channel";
        String info = "";
        String num = request.getHeader("num");
        userId += num;
        switch (path) {
            case "/tokens":
                String token = Signing.generateConnectionToken(userId, timestamp, info);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", userId);
                    jsonObject.put("timestamp", timestamp);
                    jsonObject.put("token", token);
                    MockResponse mockResponse = new MockResponse();
                    mockResponse.setBody(jsonObject.toString());
                    mockResponse.setResponseCode(200);
                    return mockResponse;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
        return null;
    }

}
