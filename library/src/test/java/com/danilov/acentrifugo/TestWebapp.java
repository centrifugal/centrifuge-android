package com.danilov.acentrifugo;

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

/**
 * Created by semyon on 29.04.16.
 */
public class TestWebapp extends Dispatcher {

    private String secret = "very-long-secret-key";

    @Override
    public MockResponse dispatch(final RecordedRequest request) throws InterruptedException {
        String timestamp = System.currentTimeMillis() + "";
        String path = request.getPath();
        String userId = "test-user-id";
//        String channelName = "test-channel";
        String info = "";
        switch (path) {
            case "/tokens":
                SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
                Mac mac = null;
                try {
                    mac = Mac.getInstance("HmacSHA256");
                } catch (NoSuchAlgorithmException e) {}
                try {
                    mac.init(secretKeySpec);
                } catch (InvalidKeyException e) {}
                mac.update(userId.getBytes());
                mac.update(timestamp.getBytes());
                byte[] hmac = mac.doFinal(info.getBytes());
                byte[] encode = new Hex().encode(hmac);
                try {
                    String token = new String(encode, "UTF-8");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", userId);
                    jsonObject.put("timestamp", timestamp);
                    jsonObject.put("token", token);
                    MockResponse mockResponse = new MockResponse();
                    mockResponse.setBody(jsonObject.toString());
                    mockResponse.setResponseCode(200);
                    return mockResponse;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
        return null;
    }

}
