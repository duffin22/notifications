package com.duffin22.notificationapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public final int NOTIFICATION_ID = 4263;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView texty = (TextView) findViewById(R.id.textView);
        texty.setText(""+Math.random());

//        Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
//        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);


        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.big_image)).build();

        Intent playServiceIntent3 = new Intent(this, MainActivity.class);
//        playServiceIntent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingPlayServiceIntent3 = PendingIntent.getActivity(this,(int) System.currentTimeMillis(),playServiceIntent3,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Notification Alert, Click Me!");
        mBuilder.setContentText("Hi, This is Android Notification Detail!");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setContentIntent(pendingPlayServiceIntent3);
        mBuilder.setStyle(bigPictureStyle);
        mBuilder.addAction(android.R.drawable.btn_star, "Like",pendingPlayServiceIntent3);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
