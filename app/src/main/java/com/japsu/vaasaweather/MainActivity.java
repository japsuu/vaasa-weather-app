package com.japsu.vaasaweather;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.japsu.vaasaweather.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity
{

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

        //TODO: FAB stuff
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
        {
            String body = ("<small>App version: " + Constants.version + "\nDevice Manufacturer: " + Build.MANUFACTURER + "\nDevice Model: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "</small>\nLeave the info above untouched, thank you! :)\n\n<b>Please write your feedback here:</b>").replace("\n", "<br/>");
            String uriText = "mailto:japsu.honkasalo@gmail.com" + "?subject=" + Uri.encode("Feedback on Vaasa Weather app!") + "&body=" + Uri.encode(body);

            Uri data = Uri.parse(uriText);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(data);

            try
            {
                startActivity(Intent.createChooser(intent, "Send feedback"));
            }
            catch(ActivityNotFoundException e)
            {
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }
}