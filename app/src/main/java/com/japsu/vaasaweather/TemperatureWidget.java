package com.japsu.vaasaweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class TemperatureWidget extends AppWidgetProvider
{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.w("TemperatureWidget", "onUpdate called");
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, TemperatureWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(), TemperatureWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);
    }
}