package com.danilov.acentrifugo;

import com.danilov.acentrifugo.listener.ConnectionListener;
import com.danilov.acentrifugo.listener.DataMessageListener;
import com.danilov.acentrifugo.listener.SubscriptionListener;
import com.danilov.acentrifugo.message.DataMessage;
import com.danilov.acentrifugo.util.DataLock;
import com.danilov.acentrifugo.util.Signing;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by semyon on 29.04.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BasicTests {

    public GenericContainer centrifugo;

    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        centrifugo = new GenericContainer("samvimes/centrifugo-with-web:1.2")
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
    public void testConnection() throws Exception {
        String containerIpAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
        String centrifugoAddress = "ws://" + containerIpAddress + "/connection/websocket";

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
        Centrifugo centrifugo = new Centrifugo(centrifugoAddress,
                userId, null, token, timestamp);

        final DataLock<Boolean> connected = new DataLock<>();
        final DataLock<Boolean> disconnected = new DataLock<>();

        centrifugo.setConnectionListener(new ConnectionListener() {
            @Override
            public void onWebSocketOpen() {}

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
        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }


    @Test
    public void testSubscribe() throws Exception {
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
        Centrifugo centrifugo = new Centrifugo(centrifugoAddress,
                userId, null, token, timestamp);

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
        Subscription subscription = new Subscription("test-channel");
        centrifugo.subscribe(subscription);
        Assert.assertEquals("test-channel", channelSubscription.lockAndGet());

        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnected.lockAndGet());
    }

    @Test
    public void testSendReceiveMessage() throws Exception {
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
        Centrifugo centrifugo = new Centrifugo(centrifugoAddress,
                userId, null, token, timestamp);

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
        Subscription subscription = new Subscription("test-channel");
        centrifugo.subscribe(subscription);
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
