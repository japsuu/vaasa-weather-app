package com.japsu.vaasaweather;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

public class TemperatureWidgetService extends Service
{
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.w("TemperatureWidgetServce", "onStartCommand called");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for (int widgetId : allWidgetIds)
        {
            // create some random data
            int number = (new Random().nextInt(100));

            RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.temperature_widget);

            // Set the text
            remoteViews.setTextViewText(R.id.tempWidget_text, "Random: " + number);

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(), TemperatureWidget.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.tempWidget_text, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();


        super.onStartCommand(intent, 0, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.w("TemperatureWidgetServce", "onBind called");
        return null;
    }
}
