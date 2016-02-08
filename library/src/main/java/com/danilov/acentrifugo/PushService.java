package com.danilov.acentrifugo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by sammyvimes on 29.10.15.
 * Service for handling downstream messages from centrifugo.
 * Pass user id, token and token's timestamp from your web application
 * and user will be able to receive messages
 * @see <a href="https://fzambia.gitbooks.io/centrifugal/content/">Centrifugo docs</a>
 */
public class PushService extends Service {

    private static final String TAG = "ACentrifugoPushService";
    private static final String PRIVATE_CHANNEL_PREFIX = "$";

    private static final int STATE_NOT_CONNECTED = 0;

    private static final int STATE_ERROR = 1;

    private static final int STATE_CONNECTED = 2;

    private static final int STATE_DISCONNECTING = 3;

    private static final int STATE_CONNECTING = 4;

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
     * Key for intent's token  value
     */
    public static final String CHANNEL_TOKEN_EXTRA = "channelToken";

    /**
     * Key for intent's token timestamp  value
     */
    public static final String TOKEN_TIMESTAMP_EXTRA = "tokenTimestamp";

    private String host;

    private String userId;

    private String clientId;

    private String tokenTimestamp;

    private String token;

    private String channel;

    private String channelToken;

    private PushClient client;

    private HandlerThread messageThread = null;
    private Handler messageHandler = null;

    private int state = STATE_NOT_CONNECTED;

    private List<SubscribeMessage> channelsToSubscribe = new ArrayList<>();

    private List<Message> pendingMessages = new LinkedList<>();
    private final Object handlerMonitor = new Object();
    private volatile boolean hasHandler = false;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (messageHandler == null) {
            messageThread = new MessageThread("CENTRIFUGO_THREAD", new Runnable() {
                @Override
                public void run() {
                    synchronized (handlerMonitor) {
                        messageHandler = new MessageHandler(messageThread.getLooper());
                        hasHandler = true;
                    }
                    for (Message message : pendingMessages) {
                        messageHandler.sendMessage(message);
                    }
                }
            });
        }

        if (intent == null) {
            return START_NOT_STICKY;
        }

        host = intent.getStringExtra(HOST_EXTRA);
        userId = intent.getStringExtra(USERID_EXTRA);
        tokenTimestamp = intent.getStringExtra(TOKEN_TIMESTAMP_EXTRA);
        token = intent.getStringExtra(TOKEN_EXTRA);

        String channel = intent.getStringExtra(CHANNEL_EXTRA);
        String channelToken = intent.getStringExtra(CHANNEL_TOKEN_EXTRA);

        Message message = Messages.getSubscribeMessage(channel, channelToken);

        if (!hasHandler) {
            synchronized (handlerMonitor) {
                if (!hasHandler) {
                    pendingMessages.add(message);
                }
            }
        } else {
            messageHandler.sendMessage(message);
        }

        return START_STICKY;
    }

    /**
     * Starts push-listener service with given host,channel, userId, token and token's timestamp
     * @param context service starter
     * @param channel channel, which you want to subscribe this user
     * @param channelToken token of a private channel (if not private simply pass null)
     * @param host your centrifugo's host (e.g. "ws://127.0.0.1:8000/connection/websocket"
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void subscribe(@NonNull final Context context,
                             @NonNull final String host,
                             @NonNull final String channel,
                             @Nullable final String channelToken,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        Intent intent = new Intent(context, PushService.class);
        intent.putExtra(HOST_EXTRA, host);
        intent.putExtra(CHANNEL_EXTRA, channel);
        intent.putExtra(USERID_EXTRA, userId);
        intent.putExtra(TOKEN_EXTRA, token);
        intent.putExtra(CHANNEL_TOKEN_EXTRA, channelToken);
        intent.putExtra(TOKEN_TIMESTAMP_EXTRA, tokenTimestamp);
        context.startService(intent);
    }

    /**
     * Starts push-listener service with given host, userId, token and token's timestamp
     * channel must be placed in string resources with id string#centrifugo_channel
     * @param context service starter
     * @param host your centrifugo's host (e.g. "ws://127.0.0.1:8000/connection/websocket"
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void subscribe(@NonNull final Context context,
                             @NonNull final String host,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        subscribe(context, host, context.getString(R.string.centrifugo_channel), null, userId, token, tokenTimestamp);
    }

    /**
     * Starts push-listener service with given userId, token and token's timestamp
     * host and channel must be placed in string resources with ids
     * string#centrifugo_host for host
     * string#centrifugo_channel for channel
     * @param context service starter
     * @param userId id of the user, passed from web application
     * @param token user's token, passed from web application
     * @param tokenTimestamp token's timestamp, passed from web application
     */
    public static void subscribe(@NonNull final Context context,
                             @NonNull final String userId,
                             @NonNull final String token,
                             @NonNull final String tokenTimestamp) {
        subscribe(context, context.getString(R.string.centrifugo_host), userId, token, tokenTimestamp);
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
            logErrorWhen("during connection", e);
        }
    }

    /**
     * Fills JSON with connection to centrifugo info
     * Derive this class and override this method to add custom fields to JSON object
     * @param handshakeData information about WebSocket handshake
     * @param jsonObject connection message
     * @throws JSONException thrown to indicate a problem with the JSON API
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
            logErrorWhen("during message handling", e);
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
            JSONObject body = message.optJSONObject("body");
            if (body != null) {
                this.clientId = body.optString("client");
            }
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
            logErrorWhen("during subscription", e);
        }
    }


    //TODO: implement
    private void subscribe(final SubscribeMessage subscribeMessage) {
        try {
            JSONObject jsonObject = new JSONObject();
            fillSubscriptionJSON(jsonObject);

            JSONArray messages = new JSONArray();
            messages.put(jsonObject);

            client.send(messages.toString());
        } catch (JSONException e) {
            logErrorWhen("during subscription", e);
        }
    }

    /**
     * Fills JSON with subscription info
     * Derive this class and override this method to add custom fields to JSON object
     * @param jsonObject subscription message
     * @throws JSONException thrown to indicate a problem with the JSON API
     */
    protected void fillSubscriptionJSON(final JSONObject jsonObject) throws JSONException {
        jsonObject.put("uid", UUID.randomUUID().toString());
        jsonObject.put("method", "subscribe");
        JSONObject params = new JSONObject();
        params.put("channel", channel);
        if (channel.startsWith(PRIVATE_CHANNEL_PREFIX)) {
            params.put("sign", channelToken);
            params.put("client", clientId); //FIXME: здесь должен быть client id который получается из респонса о connect'е
            params.put("info", "");
        }
        jsonObject.put("params", params);
    }

    protected String getSubscriptionError(final JSONObject message) {
        return null;
    }

    public void onClose(final int code, final String reason, final boolean remote) {
        Log.i(TAG, "onClose: " + code + ", " + reason + ", " + remote);
    }

    public void logErrorWhen(final String when, final Exception ex) {
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
            try {
                this.closeBlocking();
                clientThread.interrupt();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while closing WS connection: " + e.getMessage(), e);
            }
        }

        @Override
        public void onError(final Exception ex) {
            PushService.this.onError(ex);
            try {
                this.closeBlocking();
                clientThread.interrupt();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while closing WS connection: " + e.getMessage(), e);
            }
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

    private class MessageThread extends HandlerThread {

        private Runnable onLooperPrepared;

        public MessageThread(final String name, final Runnable onLooperPrepared) {
            super(name);
            this.onLooperPrepared = onLooperPrepared;
        }

        @Override
        protected void onLooperPrepared() {
            if (onLooperPrepared != null) {
                onLooperPrepared.run();
            }
        }

    }

    private class MessageHandler extends Handler {

        public MessageHandler() {
        }

        public MessageHandler(final Callback callback) {
            super(callback);
        }

        public MessageHandler(final Looper looper) {
            super(looper);
        }

        public MessageHandler(final Looper looper, final Callback callback) {
            super(looper, callback);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case Messages.SUBSCRIBE_MESSAGE_ID:
                SubscribeMessage subscribeMessage = (SubscribeMessage) msg.obj;
                if (client == null || state != STATE_CONNECTED) {
                    client = new PushClient(URI.create(host), new Draft_17());
                    client.start();
                    channelsToSubscribe.add(subscribeMessage);
                } else {
                    subscribe(subscribeMessage);
                }
                break;
            }
        }

    }

}