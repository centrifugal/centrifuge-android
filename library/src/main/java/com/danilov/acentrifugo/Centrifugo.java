package com.danilov.acentrifugo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import android.util.Log;

import com.danilov.acentrifugo.async.Future;
import com.danilov.acentrifugo.listener.ConnectionListener;
import com.danilov.acentrifugo.listener.DataMessageListener;
import com.danilov.acentrifugo.listener.DownstreamMessageListener;
import com.danilov.acentrifugo.listener.PartyListener;
import com.danilov.acentrifugo.listener.SubscriptionListener;
import com.danilov.acentrifugo.message.DataMessage;
import com.danilov.acentrifugo.message.DownstreamMessage;
import com.danilov.acentrifugo.message.presence.JoinMessage;
import com.danilov.acentrifugo.message.presence.LeftMessage;
import com.danilov.acentrifugo.message.presence.PresenceMessage;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by semyon on 29.04.16.
 */
public class Centrifugo {

    private static final String TAG = "CentrifugoClient";

    private static final String PRIVATE_CHANNEL_PREFIX = "$";

    private static final int STATE_NOT_CONNECTED = 0;

    private static final int STATE_ERROR = 1;

    private static final int STATE_CONNECTED = 2;

    private static final int STATE_DISCONNECTING = 3;

    private static final int STATE_CONNECTING = 4;

    private String host;

    private String userId;

    private String clientId;

    private String token;

    private String tokenTimestamp;

    private Client client;

    private int state = STATE_NOT_CONNECTED;

    private List<String> subscribedChannels = new ArrayList<>();

    private List<Subscription> channelsToSubscribe = new ArrayList<>();

    @Nullable
    private DataMessageListener dataMessageListener;

    @Nullable
    private ConnectionListener connectionListener;

    @Nullable
    private SubscriptionListener subscriptionListener;

    @Nullable
    private PartyListener partyListener;

    private Map<String, DownstreamMessageListener> commandListeners = new HashMap<>();

    public Centrifugo(final String host, final String userId, final String clientId, final String token, final String tokenTimestamp) {
        this.host = host;
        this.userId = userId;
        this.clientId = clientId;
        this.token = token;
        this.tokenTimestamp = tokenTimestamp;
    }

    public void connect() {
        if (client == null || state != STATE_CONNECTED) {
            this.state = STATE_CONNECTING;
            client = new Client(URI.create(host), new Draft_17());
            client.start();
        }
    }

    public void disconnect() {
        if (client != null && state == STATE_CONNECTED) {
            client.stop();
        }
    }

    @Nullable
    public PartyListener getPartyListener() {
        return partyListener;
    }

    public void setPartyListener(@Nullable final PartyListener partyListener) {
        this.partyListener = partyListener;
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
        params.put("info", "");
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
        if (connectionListener != null) {
            connectionListener.onDisconnected(code, reason, remote);
        }
    }

    public void logErrorWhen(final String when, final Exception ex) {
        Log.e(TAG, "Error occured  " + when +  ": ", ex);
    }

    public void onError(final Exception ex) {
        Log.e(TAG, "onError: ", ex);
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

    protected void onNewMessage(final DataMessage message) {
        if (dataMessageListener != null) {
            dataMessageListener.onNewDataMessage(message);
        }
    }

    protected void onLeftMessage(final LeftMessage leftMessage) {
        if (partyListener != null) {
            partyListener.onLeave(leftMessage);
        }
    }

    protected void onJoinMessage(final JoinMessage joinMessage) {
        if (partyListener != null) {
            partyListener.onJoin(joinMessage);
        }
    }

    public void subscribe(final Subscription subscription) {
        try {
            JSONObject jsonObject = new JSONObject();
            fillSubscriptionJSON(jsonObject, subscription);

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
    protected void fillSubscriptionJSON(final JSONObject jsonObject, final Subscription subscription) throws JSONException {
        jsonObject.put("uid", UUID.randomUUID().toString());
        jsonObject.put("method", "subscribe");
        JSONObject params = new JSONObject();
        String channel = subscription.getChannel();
        params.put("channel", channel);
        if (channel.startsWith(PRIVATE_CHANNEL_PREFIX)) {
            params.put("sign", subscription.getChannelToken());
            params.put("client", clientId);
            params.put("info", subscription.getInfo());
        }
        jsonObject.put("params", params);
    }

    protected String getSubscriptionError(final JSONObject message) {
        return null;
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
            for (Subscription subscription : channelsToSubscribe) {
                subscribe(subscription);
            }
            channelsToSubscribe.clear();
            onConnected();
            return;
        }
        if (method.equals("subscribe")) {
            String subscriptionError = getSubscriptionError(message);
            if (subscriptionError != null) {
                onSubscriptionError(subscriptionError);
                return;
            }
            JSONObject body = message.optJSONObject("body");
            if (body != null) {
                String channelName = body.optString("channel");
                Boolean status = body.optBoolean("status");
                if (status) {
                    subscribedChannels.add(channelName);
                    onSubscribedToChannel(channelName);
                }
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
            String uuid = presenceMessage.getUUID();
            DownstreamMessageListener listener = commandListeners.get(uuid);
            if (listener != null) {
                listener.onDownstreamMessage(presenceMessage);
            }
            return;
        }
        onNewMessage(new DataMessage(message));
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

    private class Client extends WebSocketClient {

        private Thread clientThread;

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
            clientThread.interrupt();
        }

        @Override
        public void onError(final Exception ex) {
            Centrifugo.this.onError(ex);
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

        public void stop() {
            try {
                this.closeBlocking();
                clientThread.interrupt();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error while closing WS connection: " + e.getMessage(), e);
            }
        }

    }

}
