package com.japsu.vaasaweather;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.japsu.vaasaweather.ui.main.SectionsPagerAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //setting up notification stuff
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //setting up all the fab and feedback stuff
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
        {
            String body = ("<small>App version: " + Constants.version + "\nDevice Manufacturer: " + Build.MANUFACTURER + "\nDevice Model: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "</small>\nLeave the info above untouched, thank you! :)\n\n<b>Please write your feedback here:</b>").replace("\n", "<br/>");
            String uriText = "mailto:japsu.honkasalo@gmail.com" + "?subject=" + Uri.encode("Feedback on Vaasa Weather app!") + "&body=" + Uri.encode(body);

            Uri data = Uri.parse(uriText);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(data);

            /**
             * TESTING PURPOSES ONLY
             */
            deliverNotification(MainActivity.this);
            //Set the toast message for the "on" case
            Toast.makeText(MainActivity.this, "Alarm On!", Toast.LENGTH_LONG).show();
            /**
             * TESTING PURPOSES ONLY
             */

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
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    //TODO: Notifications
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

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
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,"Stand up notification", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifies every 15 minutes to stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}