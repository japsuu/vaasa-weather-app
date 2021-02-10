package com.japsu.vaasaweather;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver
{
    private static NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    //store context for Temperature handler
    public static Context cntxt = null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        cntxt = context;
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Fragment2.GetTemperature(true);
        //deliverNotification(context);
    }

    public static void deliverNotification(Context context, double temp)
    {
        Intent contentIntent = new Intent(cntxt, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(temp > 0)
        {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Jäätämisvaroitus")
                .setContentText("Yön aikana on ollut " + temp + " pakkasta, aja varoen!")
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        //deliver notification
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}