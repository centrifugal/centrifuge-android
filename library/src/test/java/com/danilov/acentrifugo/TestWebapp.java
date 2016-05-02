package com.danilov.acentrifugo;

import com.danilov.acentrifugo.util.Signing;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Request;

/**
 * Created by semyon on 29.04.16.
 */
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
