package com.duffin22.notifyapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

public class SecondActivity extends AppCompatActivity {
    Button butty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        butty = (Button) findViewById(R.id.button2);

        Intent i = getIntent();
        String s = i.getStringExtra("status");

        if (s.equalsIgnoreCase("yes")) {
            butty.setVisibility(View.VISIBLE);
            butty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissNotification();
                }
            });
        } else {
            butty.setVisibility(View.GONE);
        }


    }

    public void dismissNotification() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MainActivity.NOTIFICATION_ID);
        super.onBackPressed();
        finish();
    }
}
