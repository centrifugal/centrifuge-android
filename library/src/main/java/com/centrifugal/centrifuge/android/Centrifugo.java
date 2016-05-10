package com.centrifugal.centrifuge.android;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.util.Log;

import com.centrifugal.centrifuge.android.async.Future;
import com.centrifugal.centrifuge.android.config.ReconnectConfig;
import com.centrifugal.centrifuge.android.credentials.Token;
import com.centrifugal.centrifuge.android.credentials.User;
import com.centrifugal.centrifuge.android.listener.ConnectionListener;
import com.centrifugal.centrifuge.android.listener.DataMessageListener;
import com.centrifugal.centrifuge.android.listener.DownstreamMessageListener;
import com.centrifugal.centrifuge.android.listener.JoinLeaveListener;
import com.centrifugal.centrifuge.android.listener.SubscriptionListener;
import com.centrifugal.centrifuge.android.message.DataMessage;
import com.centrifugal.centrifuge.android.message.DownstreamMessage;
import com.centrifugal.centrifuge.android.message.SubscribeMessage;
import com.centrifugal.centrifuge.android.message.history.HistoryMessage;
import com.centrifugal.centrifuge.android.message.presence.JoinMessage;
import com.centrifugal.centrifuge.android.message.presence.LeftMessage;
import com.centrifugal.centrifuge.android.message.presence.PresenceMessage;
import com.centrifugal.centrifuge.android.subscription.ActiveSubscription;
import com.centrifugal.centrifuge.android.subscription.SubscriptionRequest;

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
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public class Centrifugo {

    private static final String TAG = "CentrifugoClient";

    private static final String PRIVATE_CHANNEL_PREFIX = "$";

    private static final int STATE_NOT_CONNECTED = 0;

    private static final int STATE_ERROR = 1;

    private static final int STATE_CONNECTED = 2;

    private static final int STATE_DISCONNECTING = 3;

    private static final int STATE_CONNECTING = 4;

    private String wsURI;

    private String userId;

    private String clientId;

    private String token;

    private String tokenTimestamp;

    private String info;

    @Nullable
    private ReconnectConfig reconnectConfig;

    private Client client;

    private int state = STATE_NOT_CONNECTED;

    private Map<String, ActiveSubscription> subscribedChannels = new HashMap<>();

    private List<SubscriptionRequest> channelsToSubscribe = new ArrayList<>();

    @Nullable
    private DataMessageListener dataMessageListener;

    @Nullable
    private ConnectionListener connectionListener;

    @Nullable
    private SubscriptionListener subscriptionListener;

    @Nullable
    private JoinLeaveListener joinLeaveListener;

    private Map<String, DownstreamMessageListener> commandListeners = new HashMap<>();

    protected Centrifugo(final String wsURI, final String userId, final String clientId, final String token, final String tokenTimestamp, final String info) {
        this.wsURI = wsURI;
        this.userId = userId;
        this.clientId = clientId;
        this.token = token;
        this.tokenTimestamp = tokenTimestamp;
        this.info = info;
    }

    public void connect() {
        if (client == null || (state != STATE_CONNECTED && state != STATE_CONNECTING)) {
            this.state = STATE_CONNECTING;
            client = new Client(URI.create(wsURI), new Draft_17());
            client.start();
        }
    }

    public void disconnect() {
        if (client != null && state == STATE_CONNECTED) {
            state = STATE_DISCONNECTING;
            client.stop();
        }
    }

    @Nullable
    public JoinLeaveListener getJoinLeaveListener() {
        return joinLeaveListener;
    }

    public void setJoinLeaveListener(@Nullable final JoinLeaveListener joinLeaveListener) {
        this.joinLeaveListener = joinLeaveListener;
    }

    public void setSubscriptionListener(@Nullable final SubscriptionListener subscriptionListener) {
        this.subscriptionListener = subscriptionListener;
    }

    public void setConnectionListener(@Nullable final ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setDataMessageListener(@Nullable final DataMessageListener dataMessageListener) {
        this.dataMessageListener = dataMessageListener;
    }

    /**
     * WebSocket connection successful opening handler
     * You don't need to override this method, unless you want to change
     * client's behaviour before connection
     * @param handshakeData information about WebSocket handshake
     */
    protected void onOpen(final ServerHandshake handshakeData) {
        onWebSocketOpen();
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

    public void onClose(final int code, final String reason, final boolean remote) {
        Log.i(TAG, "onClose: " + code + ", " + reason + ", " + remote);
        onDisconnected(code, reason, remote);
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
        params.put("info", info);
        params.put("token", token);
        jsonObject.put("params", params);
    }

    protected void onWebSocketOpen() {
        if (connectionListener != null) {
            connectionListener.onWebSocketOpen();
        }
    }

    protected void onConnected() {
        if (connectionListener != null) {
            connectionListener.onConnected();
        }
    }

    protected void onDisconnected(final int code, final String reason, final boolean remote) {
        state = STATE_NOT_CONNECTED;
        for (ActiveSubscription activeSubscription : subscribedChannels.values()) {
            activeSubscription.setConnected(false);
        }
        if (connectionListener != null) {
            connectionListener.onDisconnected(code, reason, remote);
        }
        //connection closed by remote host or was lost
        if (remote) {
            if (reconnectConfig != null) {
                //reconnect enabled
                if (reconnectConfig.shouldReconnect()) {
                    reconnectConfig.incReconnectCount();
                    long reconnectDelay = reconnectConfig.getReconnectDelay();
                    scheduleReconnect(reconnectDelay);
                }
            }
        }
    }

    public void logErrorWhen(final String when, final Exception ex) {
        Log.e(TAG, "Error occured  " + when +  ": ", ex);
    }

    public void onError(final Exception ex) {
        Log.e(TAG, "onError: ", ex);
        state = STATE_ERROR;
    }

    protected void onSubscriptionError(@Nullable final String subscriptionError) {
        if (subscriptionListener != null) {
            subscriptionListener.onSubscriptionError(null, subscriptionError); //FIXME: rewrite using channel name
        }
    }

    protected void onSubscribedToChannel(@Nonnull final String channelName) {
        if (subscriptionListener != null) {
            subscriptionListener.onSubscribed(channelName);
        }
    }

    protected void onNewMessage(final DataMessage dataMessage) {
        String uuid = dataMessage.getUUID();
        //update last message id
        ActiveSubscription activeSubscription = subscribedChannels.get(dataMessage.getChannel());
        if (activeSubscription != null) {
            activeSubscription.updateLastMessage(uuid);
        }
        if (dataMessageListener != null) {
            dataMessageListener.onNewDataMessage(dataMessage);
        }
    }

    protected void onLeftMessage(final LeftMessage leftMessage) {
        if (joinLeaveListener != null) {
            joinLeaveListener.onLeave(leftMessage);
        }
    }

    protected void onJoinMessage(final JoinMessage joinMessage) {
        if (joinLeaveListener != null) {
            joinLeaveListener.onJoin(joinMessage);
        }
    }

    public void subscribe(@Nonnull final SubscriptionRequest subscriptionRequest) {
        subscribe(subscriptionRequest, null);
    }

    public void subscribe(final SubscriptionRequest subscriptionRequest, @Nullable final String lastMessageId) {
        if (state != STATE_CONNECTED) {
            channelsToSubscribe.add(subscriptionRequest);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            String uuid = fillSubscriptionJSON(jsonObject, subscriptionRequest, lastMessageId);
            commandListeners.put(uuid, new DownstreamMessageListener() {
                @Override
                public void onDownstreamMessage(final DownstreamMessage message) {
                    SubscribeMessage subscribeMessage = (SubscribeMessage) message;
                    String subscriptionError = subscribeMessage.getError();
                    if (subscriptionError != null) {
                        onSubscriptionError(subscriptionError);
                        return;
                    }
                    String channelName = subscribeMessage.getChannel();
                    Boolean status = subscribeMessage.getStatus();
                    if (status != null && status) {
                        if (channelName != null) {
                            ActiveSubscription activeSubscription;
                            String channel = subscriptionRequest.getChannel();
                            if (subscribedChannels.containsKey(channel)) {
                                activeSubscription = subscribedChannels.get(channel);
                            } else {
                                activeSubscription = new ActiveSubscription(subscriptionRequest);
                                subscribedChannels.put(channel, activeSubscription);
                            }
                            //mark as connected
                            activeSubscription.setConnected(true);
                            onSubscribedToChannel(channelName);
                        }
                    }
                    JSONArray recoveredMessages = subscribeMessage.getRecoveredMessages();
                    if (recoveredMessages != null) {
                        for (int i = 0; i < recoveredMessages.length(); i++) {
                            JSONObject messageObj = recoveredMessages.optJSONObject(i);
                            DataMessage dataMessage = DataMessage.fromBody(messageObj);
                            onNewMessage(dataMessage);
                        }
                    }
                }
            });
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
     * @param subscriptionRequest request for subscription
     * @param lastMessageId id of last message
     * @throws JSONException thrown to indicate a problem with the JSON API
     * @return uid of this command
     */
    protected String fillSubscriptionJSON(final JSONObject jsonObject, final SubscriptionRequest subscriptionRequest, @Nullable final String lastMessageId) throws JSONException {
        String uuid = UUID.randomUUID().toString();
        jsonObject.put("uid", uuid);
        jsonObject.put("method", "subscribe");
        JSONObject params = new JSONObject();
        String channel = subscriptionRequest.getChannel();
        params.put("channel", channel);
        if (channel.startsWith(PRIVATE_CHANNEL_PREFIX)) {
            params.put("sign", subscriptionRequest.getChannelToken());
            params.put("client", clientId);
            params.put("info", subscriptionRequest.getInfo());
        }
        if (lastMessageId != null) {
            params.put("last", lastMessageId);
            params.put("recover", true);
        }
        jsonObject.put("params", params);
        return uuid;
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
    protected void onMessage(@Nonnull final JSONObject message) {
        String method = message.optString("method", "");
        if (method.equals("connect")) {
            JSONObject body = message.optJSONObject("body");
            if (body != null) {
                this.clientId = body.optString("client");
            }
            this.state = STATE_CONNECTED;
            for (SubscriptionRequest subscriptionRequest : channelsToSubscribe) {
                subscribe(subscriptionRequest);
            }
            channelsToSubscribe.clear();
            for (ActiveSubscription activeSubscription : subscribedChannels.values()) {
                subscribe(activeSubscription.getInitialRequest(), activeSubscription.getLastMessageId());
            }
            onConnected();
            return;
        }
        if (method.equals("subscribe")) {
            SubscribeMessage subscribeMessage = new SubscribeMessage(message);
            String uuid = subscribeMessage.getRequestUUID();
            DownstreamMessageListener listener = commandListeners.get(uuid);
            if (listener != null) {
                listener.onDownstreamMessage(subscribeMessage);
            }
            return;
        }
        if (method.equals("join")) {
            JoinMessage joinMessage = new JoinMessage(message);
            onJoinMessage(joinMessage);
            return;
        }
        if (method.equals("leave")) {
            LeftMessage leftMessage = new LeftMessage(message);
            onLeftMessage(leftMessage);
            return;
        }
        if (method.equals("presence")) {
            PresenceMessage presenceMessage = new PresenceMessage(message);
            String uuid = presenceMessage.getRequestUUID();
            DownstreamMessageListener listener = commandListeners.get(uuid);
            if (listener != null) {
                listener.onDownstreamMessage(presenceMessage);
            }
            return;
        }
        if (method.equals("history")) {
            HistoryMessage historyMessage = new HistoryMessage(message);
            String uuid = historyMessage.getRequestUUID();
            DownstreamMessageListener listener = commandListeners.get(uuid);
            if (listener != null) {
                listener.onDownstreamMessage(historyMessage);
            }
            return;
        }
        DataMessage dataMessage = new DataMessage(message);
        onNewMessage(dataMessage);
    }

    public Future<HistoryMessage> requestHistory(final String channelName) {
        JSONObject jsonObject = new JSONObject();
        String commandId = UUID.randomUUID().toString();
        try {
            jsonObject.put("uid", commandId);
            jsonObject.put("method", "history");
            JSONObject params = new JSONObject();
            params.put("channel", channelName);
            jsonObject.put("params", params);
        } catch (JSONException e) {
            //FIXME error handling
        }
        final Future<HistoryMessage> historyMessage = new Future<>();
        //don't let block client's thread
        historyMessage.setRestrictedThread(client.getClientThread());
        commandListeners.put(commandId, new DownstreamMessageListener() {
            @Override
            public void onDownstreamMessage(final DownstreamMessage message) {
                historyMessage.setData((HistoryMessage) message);
            }
        });
        client.send(jsonObject.toString());
        return historyMessage;
    }

    public Future<PresenceMessage> requestPresence(final String channelName) {
        JSONObject jsonObject = new JSONObject();
        String commandId = UUID.randomUUID().toString();
        try {
            jsonObject.put("uid", commandId);
            jsonObject.put("method", "presence");
            JSONObject params = new JSONObject();
            params.put("channel", channelName);
            jsonObject.put("params", params);
        } catch (JSONException e) {
            //FIXME error handling
        }
        final Future<PresenceMessage> presenceMessage = new Future<>();
        //don't let block client's thread
        presenceMessage.setRestrictedThread(client.getClientThread());
        commandListeners.put(commandId, new DownstreamMessageListener() {
            @Override
            public void onDownstreamMessage(final DownstreamMessage message) {
                presenceMessage.setData((PresenceMessage) message);
            }
        });
        client.send(jsonObject.toString());
        return presenceMessage;
    }

    private void scheduleReconnect(@Nonnegative final long delay) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                connect();
                timer.cancel();
                this.cancel();
            }
        }, delay);
    }

    public void setReconnectConfig(@Nullable final ReconnectConfig reconnectConfig) {
        this.reconnectConfig = reconnectConfig;
    }

    private class Client extends WebSocketClient {

        private Thread clientThread;

        private ExecutorService executor = Executors.newSingleThreadExecutor();

        public Client(final URI serverURI, final Draft draft) {
            super(serverURI, draft);
            clientThread = new Thread(this, "Centrifugo");
        }

        public Thread getClientThread() {
            return clientThread;
        }

        @Override
        public void onOpen(final ServerHandshake handshakedata) {
            Centrifugo.this.onOpen(handshakedata);
        }

        /**
         * Internal handler of message from WebSocket, which can be either
         * JSONObject and JSONArray
         * @param message string frame
         */
        @Override
        public void onMessage(final String message) {
            try {
                Object object = new JSONTokener(message).nextValue();
                if (object instanceof JSONObject) {
                    JSONObject messageObj = (JSONObject) object;
                    Centrifugo.this.onMessage(messageObj);
                } else if (object instanceof JSONArray) {
                    JSONArray messageArray = new JSONArray(message);
                    for (int i = 0; i < messageArray.length(); i++) {
                        JSONObject messageObj = messageArray.optJSONObject(i);
                        Centrifugo.this.onMessage(messageObj);
                    }
                }
            } catch (JSONException e) {
                logErrorWhen("during message handling", e);
            }
        }

        @Override
        public void onClose(final int code, final String reason, final boolean remote) {
            Centrifugo.this.onClose(code, reason, remote);
        }

        @Override
        public void onError(final Exception ex) {
            Centrifugo.this.onError(ex);
            try {
                this.closeBlocking();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while closing WS connection: " + e.getMessage(), e);
            }
        }

        public void start() {
            clientThread.start();
        }

        public void stop() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Client.this.closeBlocking();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Error while closing WS connection: " + e.getMessage(), e);
                    }
                }
            });
        }

        @Override
        public void send(final byte[] data) throws NotYetConnectedException {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Client.super.send(data);
                }
            });
        }

        @Override
        public void send(final String text) throws NotYetConnectedException {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Client.super.send(text);
                }
            });
        }

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
            Centrifugo centrifugo = new Centrifugo(wsURI, user.getUser(), user.getClient(), token.getToken(), token.getTokenTimestamp(), info);
            centrifugo.setReconnectConfig(reconnectConfig);
            return centrifugo;
        }

    }

}
