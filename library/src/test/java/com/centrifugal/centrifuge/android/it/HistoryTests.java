package com.centrifugal.centrifuge.android.it;

import com.centrifugal.centrifuge.BuildConfig;
import com.centrifugal.centrifuge.android.Centrifugo;
import com.centrifugal.centrifuge.android.credentials.Token;
import com.centrifugal.centrifuge.android.credentials.User;
import com.centrifugal.centrifuge.android.subscription.SubscriptionRequest;
import com.centrifugal.centrifuge.android.TestWebapp;
import com.centrifugal.centrifuge.android.listener.ConnectionListener;
import com.centrifugal.centrifuge.android.listener.DataMessageListener;
import com.centrifugal.centrifuge.android.listener.SubscriptionListener;
import com.centrifugal.centrifuge.android.message.DataMessage;
import com.centrifugal.centrifuge.android.message.history.HistoryItem;
import com.centrifugal.centrifuge.android.message.history.HistoryMessage;
import com.centrifugal.centrifuge.android.util.DataLock;
import com.centrifugal.centrifuge.android.util.Signing;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This file is part of ACentrifugo.
 * <p/>
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by Semyon on 04.05.2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class HistoryTests {

    public GenericContainer centrifugo;

    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        centrifugo = new GenericContainer("samvimes/centrifugo-with-web:1.3")
                .withExposedPorts(8000);
        centrifugo.start();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void afterMethod() throws Exception {
        mockWebServer.shutdown();
        centrifugo.stop();
    }

    @Test
    public void testHistoryRequest() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";
        String centrifugoApiAddress = "http://" + containerIpAddress + "/api/";

        mockWebServer.setDispatcher(new TestWebapp());
        String url = mockWebServer.url("/tokens").toString();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request build = new Request.Builder().url(url).build();
        Response execute = okHttpClient.newCall(build).execute();
        String body = execute.body().string();
        JSONObject loginObject = new JSONObject(body);
        String userId = loginObject.optString("userId");
        String timestamp = loginObject.optString("timestamp");
        String token = loginObject.optString("token");
        Centrifugo centrifugo = new Centrifugo.Builder(centrifugoAddress)
                .setUser(new User(userId, null))
                .setToken(new Token(token, timestamp))
                .build();

        final DataLock<Boolean> connected = new DataLock<>();
        final DataLock<Boolean> disconnected = new DataLock<>();

        centrifugo.setConnectionListener(new ConnectionListener() {
            @Override
            public void onWebSocketOpen() {
            }

            @Override
            public void onConnected() {
                connected.setData(true);
            }

            @Override
            public void onDisconnected(final int code, final String reason, final boolean remote) {
                disconnected.setData(!remote);
            }
        });

        centrifugo.connect();
        Assert.assertTrue("Failed to connect to centrifugo", connected.lockAndGet());


        final DataLock<String> channelSubscription = new DataLock<>();
        centrifugo.setSubscriptionListener(new SubscriptionListener() {
            @Override
            public void onSubscribed(final String channelName) {
                channelSubscription.setData(channelName);
            }

            @Override
            public void onUnsubscribed(final String channelName) {

            }

            @Override
            public void onSubscriptionError(final String channelName, final String error) {

            }
        });
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("test-channel");
        centrifugo.subscribe(subscriptionRequest);
        Assert.assertEquals("test-channel", channelSubscription.lockAndGet());

        final DataLock<DataMessage> messageData = new DataLock<>();
        centrifugo.setDataMessageListener(new DataMessageListener() {
            @Override
            public void onNewDataMessage(final DataMessage message) {
                messageData.setData(message);
            }
        });

        MediaType appJson = MediaType.parse("application/json");
        JSONObject msg = new JSONObject();
        msg.put("input", "Hello world");
        JSONObject jsonObject = sendMessageJson("test-channel", msg);
        String apiSign = Signing.generateApiToken(jsonObject.toString());
        Request post = new Request.Builder()
                .url(centrifugoApiAddress)
                .method("POST",
                        RequestBody.create(appJson, jsonObject.toString()))
                .header("X-API-Sign", apiSign)
                .header("Content-type", "application/json")
                .build();
        Response postMessage = okHttpClient.newCall(post).execute();
        Assert.assertEquals(200, postMessage.code());
        DataMessage dataMessage = messageData.lockAndGet();
        String input = dataMessage.getData().toString();
        Assert.assertEquals(msg.toString(), input);

        msg = new JSONObject();
        msg.put("input", "Hello world#2");
        jsonObject = sendMessageJson("test-channel", msg);
        apiSign = Signing.generateApiToken(jsonObject.toString());
        post = new Request.Builder()
                .url(centrifugoApiAddress)
                .method("POST",
                        RequestBody.create(appJson, jsonObject.toString()))
                .header("X-API-Sign", apiSign)
                .header("Content-type", "application/json")
                .build();
        postMessage = okHttpClient.newCall(post).execute();
        Assert.assertEquals(200, postMessage.code());

        HistoryMessage historyMessage = centrifugo.requestHistory("test-channel").blockingGet();
        Assert.assertNotNull(historyMessage);
        List<HistoryItem> messages = historyMessage.getMessages();
        Assert.assertEquals(2, messages.size());
        JSONObject data1 = messages.get(0).getData();
        Assert.assertNotNull(data1);
        JSONObject data2 = messages.get(1).getData();
        Assert.assertNotNull(data2);
        Assert.assertEquals("{\"input\":\"Hello world#2\"}", data1.toString());
        Assert.assertEquals("{\"input\":\"Hello world\"}", data2.toString());

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    private JSONObject sendMessageJson(final String channel, final JSONObject message) {
        JSONObject sendMessageJson = new JSONObject();
        try {
            sendMessageJson.put("method", "publish");
            JSONObject params = new JSONObject();
            params.put("channel", channel);
            params.put("data", message);
            sendMessageJson.put("params", params);
        } catch (JSONException e) {}
        return sendMessageJson;
    }

}
