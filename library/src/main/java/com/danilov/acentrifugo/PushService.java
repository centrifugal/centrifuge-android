package com.danilov.acentrifugo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.util.UUID;

/**
 * Created by semyon on 29.10.15.
 */
public class PushService extends Service implements IPushService {

    public static final String HOST_EXTRA = "host";
    public static final String USERID_EXTRA = "userId";
    public static final String TOKEN_TIMESTAMP_EXTRA = "tokenTimestamp";
    public static final String TOKEN_EXTRA = "token";
    public static final String CHANNEL_EXTRA = "channel";

    private Handler handler = new Handler();

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

    public static void start(final Context context, final String host, final String userId, final String tokenTimestamp, final String token, final String channel) {
        Intent intent = new Intent(context, PushService.class);
        intent.putExtra(HOST_EXTRA, host);
        intent.putExtra(TOKEN_EXTRA, token);
        intent.putExtra(TOKEN_TIMESTAMP_EXTRA, tokenTimestamp);
        intent.putExtra(USERID_EXTRA, userId);
        intent.putExtra(CHANNEL_EXTRA, channel);
        context.startService(intent);
    }

    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        //sending connect
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", UUID.randomUUID().toString());
            jsonObject.put("method", "connect");

            JSONObject params = new JSONObject();
            params.put("user", userId);
            params.put("timestamp", tokenTimestamp);
            params.put("info", "");
            params.put("token", token);
            jsonObject.put("params", params);

            JSONArray messages = new JSONArray();
            messages.put(jsonObject);

            client.send(messages.toString());

        } catch (JSONException e) {

        }
    }

    public void onConnected() {

    }

    @Override
    public void onMessage(final String message) {
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
            onError(e);
        }
    }

    private void onMessage(final JSONObject jsonObject) {
        String method = jsonObject.optString("method", "");
        if (method.equals("connect")) {
            subscribe();
            return;
        }
        if (method.equals("subscribe")) {
            //TODO: check subscription success
            return;
        }
        JSONObject body = jsonObject.optJSONObject("body");
        String packageName = getPackageName();
        Intent intent = new Intent(packageName + ".action.CENTRIFUGO_PUSH");
        intent.putExtra("body", body.toString());
        sendBroadcast(intent, packageName + ".permission.CENTRIFUGO_PUSH");
    }

    private void subscribe() {
        //sending connect
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", UUID.randomUUID().toString());
            jsonObject.put("method", "subscribe");

            JSONObject params = new JSONObject();
            params.put("channel", channel);
            jsonObject.put("params", params);

            JSONArray messages = new JSONArray();
            messages.put(jsonObject);

            client.send(messages.toString());

        } catch (JSONException e) {
            onError(e);
        }
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        Log.d("PUSH", "onClose: " + code + ", " + reason + ", " + remote);
    }

    @Override
    public void onError(final Exception ex) {
        Log.e("PUSH", "onError: ", ex);
    }

    private class PushClient extends WebSocketClient {

        private Thread clientThread;

        public PushClient(final URI serverURI, final Draft draft) {
            super(serverURI, draft);
            clientThread = new Thread() {
                @Override
                public void run() {
                    String error = "";
                    try {
                        boolean success = connectBlocking();
                        if (success) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onConnected();
                                }
                            });
                            return;
                        }
                        error = "Unknow error";
                    } catch (InterruptedException e) {
                        error = e.getMessage();
                    }
                    Log.e("PUSH", "Error while connecting: " + error);
                }
            };
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
