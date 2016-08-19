package com.duffin22.notifyapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static int NOTIFICATION_ID = 426;
    public String string;
    TextView texty;

    @Override
    protected void onResume() {
        super.onResume();
        texty = (TextView) findViewById(R.id.textView);
        texty.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        texty = (TextView) findViewById(R.id.textView);
        texty.setVisibility(View.GONE);

        Button butty = (Button) findViewById(R.id.button);
        butty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {
                    string = "yes";
                    makeNotification("yes");
                } else {
                    string = "no";
                    makeNotification("no");
                }
                texty.setVisibility(View.VISIBLE);
            }
        });

    }

    public void makeNotification(String string) {
        String message;
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:1-425-417-4643"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(this);
            return;
        }
        Intent intent = new Intent(this, SecondActivity.class);

        NotificationCompat.BigPictureStyle wifiStyle = new NotificationCompat.BigPictureStyle();
        if (string.equalsIgnoreCase("yes")) {
            wifiStyle.bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.yes_wifi)).build();
            message = "Wifi connection available";
            intent.putExtra("status", "yes");
        } else if (string.equalsIgnoreCase("no")) {
            wifiStyle.bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.no_wifi)).build();
            message = "Wifi connection not available";
            intent.putExtra("status", "no");
        } else { return; }

        PendingIntent pendingIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(),intent,0);

        PendingIntent pendingPhoneIntent = PendingIntent.getActivity(this,(int) System.currentTimeMillis(),phoneIntent,0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_wifi_black_24dp);
        mBuilder.setContentTitle("Wifi Status");
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(wifiStyle);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.addAction(R.drawable.ic_android_black_24dp,"Wifi Info",pendingIntent);
        mBuilder.addAction(R.drawable.ic_call_black_24dp,"Call Random",pendingPhoneIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "We dont have them");
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.CALL_PHONE,
                            Manifest.permission.READ_PHONE_STATE}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (string.equalsIgnoreCase("yes")) {
            makeNotification("yes");
        } else {
            makeNotification("no");
        }

    }
}
