package com.maktab.mahdi.pushnotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;

public class MyPushListener extends PusheListenerService {

    public static String MY_PREFS_NAME = "pushPreferences";

    @Override
    public void onMessageReceived(JSONObject customContent, JSONObject pushMessage) {
        if (customContent == null || customContent.length() == 0)
            return; //json is empty
        Log.i("Pushe", "Custom json Message: " + customContent.toString()); //print json to logCat
        //Do something with json
        try {
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String sender = customContent.getString("sender");
            String newMessage = customContent.getString("message");
            String message = prefs.getString("message", "") + sender + ":  " + newMessage + "\n";
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("sender", sender);
            editor.putString("message", message);
            editor.apply();
            EventBus.getDefault().post(new MessageEvent(sender, message));

            //  Log.i("Pushe", "Json Message sender: " + sender + " message: " + message);

            if (!MainFragment.active) {
                showNotification(sender, newMessage);
            }
        } catch (JSONException e) {
            android.util.Log.e("TAG", "Exception in parsing json", e);
        }
    }

    private void showNotification(String sender, String message) {
        Intent i = MainActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        final NotificationCompat.Builder mBuilder;

        mBuilder = new NotificationCompat.Builder(this, getResources()
                .getString(R.string.channel_id));

        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(sender)
                .setContentText(message)
                .setContentIntent(pi)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/"
                        + R.raw.ping_tone))
                .setVibrate(new long[]{0, 1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 300, 100)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(Notification.PRIORITY_MAX);

        Notification notification = mBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);

    }
}
