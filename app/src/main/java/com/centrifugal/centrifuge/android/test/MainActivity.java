package com.centrifugal.centrifuge.android.test;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.centrifugal.centrifuge.android.Centrifugo;
import com.centrifugal.centrifuge.android.credentials.Token;
import com.centrifugal.centrifuge.android.credentials.User;
import com.centrifugal.centrifuge.android.listener.ConnectionListener;
import com.centrifugal.centrifuge.android.listener.DataMessageListener;
import com.centrifugal.centrifuge.android.listener.JoinLeaveListener;
import com.centrifugal.centrifuge.android.listener.SubscriptionListener;
import com.centrifugal.centrifuge.android.message.DataMessage;
import com.centrifugal.centrifuge.android.message.presence.JoinMessage;
import com.centrifugal.centrifuge.android.message.presence.LeftMessage;
import com.centrifugal.centrifuge.android.subscription.SubscriptionRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText addressEditText;

    private EditText userNameEditText;

    private EditText channelEditText;

    private ListView listView;

    private List<DataMessage> dataMessageList = new ArrayList<>();
    private Adapter stringArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                login();
            }
        });
        findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String channel = channelEditText.getText().toString();
                centrifugo.subscribe(new SubscriptionRequest(channel));
            }
        });
        addressEditText = (EditText) findViewById(R.id.address);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        channelEditText = (EditText) findViewById(R.id.channel_name);
        listView = (ListView) findViewById(R.id.messages);
        stringArrayAdapter = new Adapter();
        listView.setAdapter(stringArrayAdapter);
    }

    private Centrifugo centrifugo;

    private void login() {
        String address = addressEditText.getText().toString();
        if (!address.contains("http")) {
            address = "http://" + address;
        }
        final String user = userNameEditText.getText().toString();
        final String channel = "updates_76b6ce932584f4f74abff7fc891afde9f50d2e50";
        final String fAddress = address;
        new Thread() {
            @Override
            public void run() {
                String userId = "1";
                String timestamp = "1518775099";
                String token = "2a26aa384091da7b4fbf96ed7ff644b3def449a1cad3a1a41b0d8c4e0fb2d65b";
                String centrifugoAddress = "ws://centrifugo.tasksamurai.com/connection/websocket";
                centrifugo = new Centrifugo.Builder(centrifugoAddress)
                        .setUser(new User(userId, null))
                        .setToken(new Token(token, timestamp))
                        .build();
                centrifugo.subscribe(new SubscriptionRequest(channel));
                centrifugo.setConnectionListener(new ConnectionListener() {
                    @Override
                    public void onWebSocketOpen() {

                    }

                    @Override
                    public void onConnected() {
                        message("Connected to Centrifugo!", "Now subscribe to channel");
                    }

                    @Override
                    public void onDisconnected(final int code, final String reason, final boolean remote) {
                        message("Disconnected from Centrifugo.", "Bye-bye");
                    }
                });
                centrifugo.setSubscriptionListener(new SubscriptionListener() {
                    @Override
                    public void onSubscribed(final String channelName) {
                        message("Just subscribed to " + channelName, "Awaiting messages");
                    }

                    @Override
                    public void onUnsubscribed(final String channelName) {
                        message("Unsubscribed from " + channelName, "Bye");
                    }

                    @Override
                    public void onSubscriptionError(final String channelName, final String error) {
                        error("Failed to subscribe to " + channelName + ", cause: " + error);
                    }

                });
                centrifugo.setDataMessageListener(new DataMessageListener() {
                    @Override
                    public void onNewDataMessage(final DataMessage message) {
                        Log.e("message",message.getBody().toString());
                        showMessage(message);
                    }
                });
                centrifugo.setJoinLeaveListener(new JoinLeaveListener() {
                    @Override
                    public void onJoin(final JoinMessage joinMessage) {
                        message(joinMessage.getUser(), " just joined " + joinMessage.getChannel());
                    }

                    @Override
                    public void onLeave(final LeftMessage leftMessage) {
                        message(leftMessage.getUser(), " just left " + leftMessage.getChannel());
                    }
                });
                centrifugo.connect();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        if (centrifugo != null) {
            centrifugo.disconnect();
        }
        super.onStop();
    }

    private void showMessage(final DataMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stringArrayAdapter.add(message);
            }
        });
    }

    private class Adapter extends BaseAdapter<Holder, DataMessage> {

        private List<DataMessage> dataMessages = new ArrayList<>();

        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        public Adapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_2);
        }

        @Override
        public int getCount() {
            return dataMessages.size();
        }

        public void add(final DataMessage dataMessage) {
            dataMessages.add(dataMessage);
            stringArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            DataMessage dataMessage = dataMessages.get(position);
            String date = sdf.format(dataMessage.getTimestamp());
            String channel = dataMessage.getChannel();
            String upper = date + " [" + channel + "]";
            String lower = "";
            String data = dataMessage.getData();
            if (data != null) {
                lower = data;
            }
            holder.tv1.setText(upper);
            holder.tv2.setText(lower);
        }

        @Override
        public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
            return new Holder(v);
        }

    }

    private class Holder extends BaseAdapter.BaseHolder {

        TextView tv1;
        TextView tv2;

        protected Holder(final View view) {
            super(view);
            tv1 = findViewById(android.R.id.text1);
            tv2 = findViewById(android.R.id.text2);
        }
    }

    private void error(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar
                        .make(findViewById(R.id.main), "Error: " + error, Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void message(final String messageMain, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SpannableStringBuilder snackbarText = new SpannableStringBuilder();
                snackbarText.append(messageMain);
                snackbarText.setSpan(new ForegroundColorSpan(0xFF00DD00), 0, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                snackbarText.append(" ").append(message);
                Snackbar.make(findViewById(R.id.main), snackbarText, Snackbar.LENGTH_LONG).show();
            }
        });
    }

}
