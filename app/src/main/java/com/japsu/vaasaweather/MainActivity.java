package com.japsu.vaasaweather;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;

import com.japsu.vaasaweather.ui.main.SectionsPagerAdapter;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    static AlarmManager alarmManager = null;
    static PendingIntent notifyPendingIntent = null;
    static Intent notifyIntent = null;
    static Context context;
    public static CustomViewPager viewPager = null;

    private static boolean isFirstStart = true;
    private static NotificationManager mNotificationManager;
    private static UiModeManager uiModeManager;
    public static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        //ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //get the context
        MainActivity.context = getApplicationContext();

        //setting up notification stuff
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //setting up night mode switch stuff
        uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);

        //setting up all the fab and feedback stuff
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
        {
            /*
            String body = ("<small>App version: " + Constants.version + "\nDevice Manufacturer: " + Build.MANUFACTURER + "\nDevice Model: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "</small>\nLeave the info above untouched, thank you! :)\n\n<b>Please write your feedback here:</b>").replace("\n", "<br/>");
            String uriText = "mailto:japsu.honkasalo@gmail.com" + "?subject=" + Uri.encode("Feedback on Vaasa Weather app!") + "&body=" + Uri.encode(body);

            Uri data = Uri.parse(uriText);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(data);*/

            String version = null;
            int versionCode = 0;

            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                version = pInfo.versionName;
                versionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"japsu.honkasalo@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Vaasa Weather app!");
            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<small>App version: " + version + versionCode + "<br/>Device Manufacturer: " + Build.MANUFACTURER + "<br/>Device Model: " + Build.MODEL + "<br/>Android Version: " + Build.VERSION.RELEASE + "</small><br/>Leave the info above untouched, thank you! :)<br/><br/><b>Please write your feedback here:</b>"));

            try
            {
                startActivity(Intent.createChooser(intent, "Send feedback"));
            }
            catch(ActivityNotFoundException e)
            {
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //Notification stuff here too
        createNotificationChannel();
        notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //nightmode stuff
        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean nightmodeOn = prefs.getBoolean("nightmodeOn", false);
        if(nightmodeOn)
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //Get the data on the first start
        if(isFirstStart)
        {
            FirstStart();
        }
    }

    private void FirstStart()
    {
        isFirstStart = false;
        Fragment2.GetTemp();
        Fragment3.GetLevel();
    }

    static void onNotifCheckedChanged(boolean checked)
    {
        SharedPreferences prefs = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("notificationsOn", checked);
        edit.apply();

        //long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        //long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        //long repeatInterval = AlarmManager.INTERVAL_DAY;
        long triggerTime = calendar.getTimeInMillis();

        // To avoid firing the alarm if the time is passed while setting
        if (System.currentTimeMillis() > triggerTime)
        {
            triggerTime = triggerTime + 24 * 60 * 60 * 1000;
        }

        if(alarmManager != null)
        {
            if(checked)
            {
                //Log.d("NOTIFICATIONS", "Notifications are now turned on!");
                //Toast toast = Toast.makeText(context, "Notifications are now turned on!", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 60);
                //toast.show();
                //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, notifyPendingIntent);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 24 * 60 * 60 * 1000, notifyPendingIntent);
            }
            else
            {
                mNotificationManager.cancelAll();
                //Log.d("NOTIFICATIONS", "Notifications are now turned off!");
                //Toast toast = Toast.makeText(context, "Notifications are now turned off!", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 60);
                //toast.show();
                alarmManager.cancel(notifyPendingIntent);
            }
        }
    }

    static void onNightCheckedChanged(boolean checked)
    {
        SharedPreferences prefs = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("nightmodeOn", checked);
        edit.apply();

        if(checked)
        {
            //Log.d("NIGHTMODE", "Nightmode is now turned on!");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else
        {
            //Log.d("NIGHTMODE", "Nightmode is now turned off!");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel()
    {
        // Create a notification manager object.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,"Freezing notification", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifies every day if it's been freezing outside");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}