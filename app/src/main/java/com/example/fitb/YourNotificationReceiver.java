package com.example.fitb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class YourNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Build and send the notification
        int notificationId = 1;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "FitB")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("STAY HYDRATED")
                .setContentText("Did you drink enough water today??")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }


}
