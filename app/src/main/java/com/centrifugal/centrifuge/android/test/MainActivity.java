package com.centrifugal.centrifuge.android.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.centrifugal.centrifuge.android.PushService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final Context context = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pi = sharedPreferences.getString("PUSH_ID", "");
        String token = sharedPreferences.getString("PUSH_TOKEN", "");
        String ts = sharedPreferences.getLong("PUSH_TIMESTAMP", 0) + "";
        String channel = context.getString(R.string.centrifugo_channel) + pi;
        if (!"".equals(pi)) {
            PushService.subscribe(context, context.getString(R.string.centrifugo_host), channel, null, pi, token, ts);
        }

    }

}
