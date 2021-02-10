package com.japsu.vaasaweather;

import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment4 extends Fragment
{
    private static Switch notificationSwitch = null;
    private static Switch nightmodeSwitch = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment4_layout, container, false);
        notificationSwitch = (Switch) view.findViewById(R.id.notificationSwitch);
        nightmodeSwitch = (Switch) view.findViewById(R.id.nightmodeSwitch);
        if(notificationSwitch != null)
        {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            {
                MainActivity.onNotifCheckedChanged(isChecked);
            });
        }
        if(nightmodeSwitch != null)
        {
            nightmodeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            {
                MainActivity.onNightCheckedChanged(isChecked);
            });
        }

        SharedPreferences prefs = this.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean alarmOn = prefs.getBoolean("notificationsOn", false);
        notificationSwitch.setChecked(alarmOn);
        boolean nightmodeOn = prefs.getBoolean("nightmodeOn", false);
        nightmodeSwitch.setChecked(nightmodeOn);

        return view;
    }
}