package com.danilov.acentrifugo;

import com.danilov.acentrifugo.listener.ConnectionListener;
import com.danilov.acentrifugo.listener.DownstreamMessageListener;
import com.danilov.acentrifugo.listener.SubscriptionListener;
import com.danilov.acentrifugo.message.DownstreamMessage;
import com.danilov.acentrifugo.util.DataLock;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by semyon on 29.04.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DockerTest {

    //    public GenericContainer dockerizedCentrifugo;

    @BeforeClass
    public static void beforeClass() throws Exception {
//        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
//        dockerClient.startContainer("centrifugo-with-web");
    }


    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void afterMethod() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void test() throws Exception {
//        String centrifugoAddress = dockerizedCentrifugo.getContainerIpAddress();
        String centrifugoAddress = "ws://192.168.99.100:32783/connection/websocket";


        mockWebServer.setDispatcher(new TestWebapp());
        String url = mockWebServer.url("/tokens").toString();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request build = new Request.Builder().url(url).build();
        Response execute = okHttpClient.newCall(build).execute();
        String body = execute.body().string();
        JSONObject jsonObject = new JSONObject(body);
        String userId = jsonObject.optString("userId");
        String timestamp = jsonObject.optString("timestamp");
        String token = jsonObject.optString("token");
        Centrifugo centrifugo = new Centrifugo(centrifugoAddress,
                userId, null, token, timestamp);

        final DataLock<Boolean> connected = new DataLock<>();

        centrifugo.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                connected.setData(true);
            }

            @Override
            public void onDisconnected(final int code, final String reason, final boolean remote) {
            }
        });

        centrifugo.start();

        Assert.assertTrue("Failed to connect to centrifugo", connected.lockAndGet(2000, TimeUnit.MILLISECONDS));


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
        centrifugo.setDownstreamMessageListener(new DownstreamMessageListener() {
            @Override
            public void onDownstreamMessage(final DownstreamMessage message) {
            }
        });

    }

}
