package com.centrifugal.centrifuge.android.it;

import com.centrifugal.centrifuge.android.BuildConfig;
import com.centrifugal.centrifuge.android.Centrifugo;
import com.centrifugal.centrifuge.android.TestWebapp;
import com.centrifugal.centrifuge.android.config.ReconnectConfig;
import com.centrifugal.centrifuge.android.credentials.Token;
import com.centrifugal.centrifuge.android.credentials.User;
import com.centrifugal.centrifuge.android.listener.ConnectionListener;
import com.centrifugal.centrifuge.android.listener.SubscriptionListener;
import com.centrifugal.centrifuge.android.subscription.SubscriptionRequest;
import com.centrifugal.centrifuge.android.util.DataLock;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by semyon on 05.05.16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class AutoReconnectTests {

    public GenericContainer centrifugo;

    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        centrifugo = new GenericContainer("samvimes/centrifugo:latest")
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
    public void testAutoReconnectAfterConnectionLoss() throws Exception {
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
        final RemoteProblemCentrifugo centrifugo = (RemoteProblemCentrifugo) new RemoteProblemCentrifugo.Builder(centrifugoAddress)
                .setUser(new User(userId, null))
                .setToken(new Token(token, timestamp))
                .setReconnectConfig(new ReconnectConfig(3, 1, TimeUnit.SECONDS))
                .build();

        final DataLock<Boolean> connected = new DataLock<>();
        final DataLock<Boolean> disconnected = new DataLock<>();
        final DataLock<Boolean> reConnected = new DataLock<>();
        final DataLock<Boolean> disconnectedAgain = new DataLock<>();
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
                //here we rely on fact, that listener is called before reconnection\
                centrifugo.setConnectionListener(new ConnectionListener() {
                    @Override
                    public void onWebSocketOpen() {
                        reConnected.setData(true);
                    }

                    @Override
                    public void onConnected() {

                    }

                    @Override
                    public void onDisconnected(final int code, final String reason, final boolean remote) {
                        disconnectedAgain.setData(true);
                    }
                });
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

        final DataLock<String> channelRESubscription = new DataLock<>();
        centrifugo.setSubscriptionListener(new SubscriptionListener() {
            @Override
            public void onSubscribed(final String channelName) {
                channelRESubscription.setData(channelName);
            }

            @Override
            public void onUnsubscribed(final String channelName) {

            }

            @Override
            public void onSubscriptionError(final String channelName, final String error) {

            }
        });

        centrifugo.disconnect();
        Assert.assertTrue("Failed to REconnect to centrifugo", reConnected.lockAndGet());
        Assert.assertEquals("Failed to REsubscruibe to test-channel", "test-channel", channelRESubscription.lockAndGet());

        centrifugo.setRemoteProblem(false);
        centrifugo.disconnect();
        Assert.assertTrue("Failed to properly disconnect to centrifugo", disconnectedAgain.lockAndGet());
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

    private static class RemoteProblemCentrifugo extends Centrifugo {

        private RemoteProblemCentrifugo(final String wsURI, final String userId, final String clientId, final String token, final String tokenTimestamp, final String info) {
            super(wsURI, userId, clientId, token, tokenTimestamp, info);
        }

        private boolean remoteProblem = true;

        public void setRemoteProblem(final boolean remoteProblem) {
            this.remoteProblem = remoteProblem;
        }

        @Override
        public void onClose(final int code, final String reason, final boolean remote) {
            super.onClose(code, reason, remoteProblem);
        }

        public static class Builder {

            @Nonnull
            private String wsURI;

            private User user;

            private Token token;

            @Nullable
            private String info;

            @Nullable
            private ReconnectConfig reconnectConfig;

            public Builder(@Nonnull final String wsURI) {
                this.wsURI = wsURI;
            }

            public Builder setToken(@Nonnull final Token token) {
                this.token = token;
                return this;
            }

            public Builder setUser(@Nonnull final User user) {
                this.user = user;
                return this;
            }

            public Builder setInfo(@Nullable final String info) {
                this.info = info;
                return this;
            }

            public Builder setReconnectConfig(@Nullable final ReconnectConfig reconnectConfig) {
                this.reconnectConfig = reconnectConfig;
                return this;
            }

            public Centrifugo build() {
                if (user == null) {
                    throw new NullPointerException("user info not provided");
                }
                if (token == null) {
                    throw new NullPointerException("token not provided");
                }
                RemoteProblemCentrifugo remoteProblemCentrifugo = new RemoteProblemCentrifugo(wsURI, user.getUser(), user.getClient(), token.getToken(), token.getTokenTimestamp(), info);
                remoteProblemCentrifugo.setReconnectConfig(reconnectConfig);
                return remoteProblemCentrifugo;
            }

        }

    }

}
