package com.danilov.acentrifugo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.util.UUID;

/**
 * Created by sammyvimes on 29.10.15.
 * Service for handling downstream messages from centrifugo.
 * Pass user id, token and token's timestamp from your web application
 * and user will be able to receive messages
 * {@see <a href="https://fzambia.gitbooks.io/centrifugal/content/">Centrifugo docs</a>}
 */
public class PushService extends Service {

    private static final String TAG = "ACentrifugoPushService";

    /**
     * Key for intent's host value
     */
    public static final String HOST_EXTRA = "host";

    /**
     * Key for intent's channel value
     */
    public static final String CHANNEL_EXTRA = "channel";

    /**
     * Key for intent's user id value
     */
    public static final String USERID_EXTRA = "userId";

    /**
     * Key for intent's token  value
     */
    public static final String TOKEN_EXTRA = "token";

    /**
     * Key for intent's token timestamp  value
     */
    public static final String TOKEN_TIMESTAMP_EXTRA = "tokenTimestamp";


    private String host;

    private String userId;

    private String tokenTimestamp;

    private String token;

    private String channel;

    private PushClient client;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        host = intent.getStringExtra(HOST_EXTRA);
        userId = intent.getStringExtra(USERID_EXTRA);
        tokenTimestamp = intent.getStringExtra(TOKEN_TIMESTAMP_EXTRA);
        token = intent.getStringExtra(TOKEN_EXTRA);
        channel = intent.getStringExtra(CHANNEL_EXTRA);

        client = new PushClient(URI.create(host), new Draft_17());
        client.start();
        return START_STICKY;
    }

    /**
     * Starts push-listener service with given host,channel, userId, token and token's timestamp
     * @param context service starter
     * @param channel channel, which you want to subscribe this user
     * @param host your centrifugo's host (e.g. "ws://127.0.0.1:8000/connection/websocket"
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void start(@NonNull final Context context,
                             @NonNull final String host,
                             @NonNull final String channel,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        Intent intent = new Intent(context, PushService.class);
        intent.putExtra(HOST_EXTRA, host);
        intent.putExtra(CHANNEL_EXTRA, channel);
        intent.putExtra(USERID_EXTRA, userId);
        intent.putExtra(TOKEN_EXTRA, token);
        intent.putExtra(TOKEN_TIMESTAMP_EXTRA, tokenTimestamp);
        context.startService(intent);
    }

    /**
     * Starts push-listener service with given host, userId, token and token's timestamp
     * channel must be placed in string resources with id {@link com.danilov.acentrifugo.R.string#centrifugo_channel}
     * @param context service starter
     * @param host your centrifugo's host (e.g. "ws://127.0.0.1:8000/connection/websocket"
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void start(@NonNull final Context context,
                             @NonNull final String host,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        start(context, host, context.getString(R.string.centrifugo_channel), userId, token, tokenTimestamp);
    }

    /**
     * Starts push-listener service with given userId, token and token's timestamp
     * host and channel must be placed in string resources with ids
     * {@link com.danilov.acentrifugo.R.string#centrifugo_host} for host
     * {@link com.danilov.acentrifugo.R.string#centrifugo_channel} for channel
     * @param context service starter
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void start(@NonNull final Context context,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        start(context, context.getString(R.string.centrifugo_host), userId, token, tokenTimestamp);
    }


    /**
     * WebSocket connection successful opening handler
     * You don't need to override this method, unless you want to change
     * client's behaviour before connection
     * @param handshakeData information about WebSocket handshake
     */
    protected void onOpen(final ServerHandshake handshakeData) {
        try {
            JSONObject jsonObject = new JSONObject();
            fillConnectionJSON(handshakeData, jsonObject);
            JSONArray messages = new JSONArray();
            messages.put(jsonObject);
            client.send(messages.toString());
        } catch (JSONException e) {
            onError("during connection", e);
        }
    }

    /**
     * Fills JSON with connection to centrifugo info
     * Derive this class and override this method to add custom fields to JSON object
     * @param handshakeData information about WebSocket handshake
     * @param jsonObject connection message
     * @throws JSONException
     */
    protected void fillConnectionJSON(final Handshakedata handshakeData, final JSONObject jsonObject) throws JSONException {
        jsonObject.put("uid", UUID.randomUUID().toString());
        jsonObject.put("method", "connect");

        JSONObject params = new JSONObject();
        params.put("user", userId);
        params.put("timestamp", tokenTimestamp);
        params.put("info", "");
        params.put("token", token);
        jsonObject.put("params", params);
    }

    /**
     * Internal handler of message from WebSocket, which can be either
     * JSONObject and JSONArray
     * @param message string frame
     */
    private void onMessage(final String message) {
        Log.d("PUSH", "Message from fugo: " + message);
        try {
            Object object = new JSONTokener(message).nextValue();
            if (object instanceof JSONObject) {
                JSONObject messageObj = (JSONObject) object;
                onMessage(messageObj);
            } else if (object instanceof JSONArray) {
                JSONArray messageArray = new JSONArray(message);
                for (int i = 0; i < messageArray.length(); i++) {
                    JSONObject messageObj = messageArray.optJSONObject(i);
                    onMessage(messageObj);
                }
            }
        } catch (JSONException e) {
            onError("during message handling", e);
        }
    }

    /**
     * Handler for messages, that does the routine of subscribing
     * and sending messages in the broadcasts
     * Only apps with permission YOUR_PACKAGE_ID.permission.CENTRIFUGO_PUSH
     * (e.g. com.example.testapp.permission.CENTRIFUGO_PUSH)
     * signed with your developer key can receive your push
     * Filter for broadcasts must be YOUR_PACKAGE_ID.action.CENTRIFUGO_PUSH
     * (e.g. com.example.testapp.action.CENTRIFUGO_PUSH)
     * You don't need to override this method, unless you want to change
     * client's behaviour after connection and before subscription
     * @param message message to handle
     */
    protected void onMessage(@NonNull final JSONObject message) {
        String method = message.optString("method", "");
        if (method.equals("connect")) {
            subscribe();
            return;
        }
        if (method.equals("subscribe")) {
            String subscriptionError = getSubscriptionError(message);
            if (subscriptionError != null) {
                onSubscriptionError(subscriptionError);
            }
            return;
        }
        JSONObject body = message.optJSONObject("body");
        String packageName = getPackageName();
        Intent intent = new Intent(packageName + ".action.CENTRIFUGO_PUSH");
        intent.putExtra("body", body.toString());
        sendBroadcast(intent, packageName + ".permission.CENTRIFUGO_PUSH");
    }

    /**
     * Subscribing to channel
     */
    private void subscribe() {
        try {
            JSONObject jsonObject = new JSONObject();
            fillSubscriptionJSON(jsonObject);

            JSONArray messages = new JSONArray();
            messages.put(jsonObject);

            client.send(messages.toString());
        } catch (JSONException e) {
            onError("during subscription", e);
        }
    }

    /**
     * Fills JSON with subscription info
     * Derive this class and override this method to add custom fields to JSON object
     * @param jsonObject subscription message
     * @throws JSONException
     */
    protected void fillSubscriptionJSON(final JSONObject jsonObject) throws JSONException {
        jsonObject.put("uid", UUID.randomUUID().toString());
        jsonObject.put("method", "subscribe");
        JSONObject params = new JSONObject();
        params.put("channel", channel);
        jsonObject.put("params", params);
    }

    protected String getSubscriptionError(final JSONObject message) {
        return null;
    }

    public void onClose(final int code, final String reason, final boolean remote) {
        Log.d(TAG, "onClose: " + code + ", " + reason + ", " + remote);
    }

    public void onError(final String when, final Exception ex) {
        Log.e(TAG, "Error occured  " + when +  ": ", ex);
    }

    public void onError(final Exception ex) {
        Log.e(TAG, "onError: ", ex);
    }

    public void onSubscriptionError(@NonNull final String subscriptionError) {
        Log.e(TAG, "subscription error: " + subscriptionError);
    }

    private class PushClient extends WebSocketClient {

        private Thread clientThread;

        public PushClient(final URI serverURI, final Draft draft) {
            super(serverURI, draft);
            clientThread = new Thread(this);
        }

        @Override
        public void onOpen(final ServerHandshake handshakedata) {
            PushService.this.onOpen(handshakedata);
        }

        @Override
        public void onMessage(final String message) {
            PushService.this.onMessage(message);
        }

        @Override
        public void onClose(final int code, final String reason, final boolean remote) {
            PushService.this.onClose(code, reason, remote);
        }

        @Override
        public void onError(final Exception ex) {
            PushService.this.onError(ex);
        }

        public void start() {
            clientThread.start();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

}