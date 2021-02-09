package com.japsu.vaasaweather;

import android.app.PendingIntent;
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
    static Switch notificationSwitch = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment4_layout, container, false);
        notificationSwitch = (Switch) view.findViewById(R.id.notificationSwitch);
        if(notificationSwitch != null)
        {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
            {
                MainActivity.onCheckedChanged(isChecked);
            });
        }
        boolean alarmUp = (PendingIntent.getBroadcast(MainActivity.context, MainActivity.NOTIFICATION_ID, MainActivity.notifyIntent, PendingIntent.FLAG_NO_CREATE) != null);
        Fragment4.setSwitchState(alarmUp);

        return view;
    }

    public static void setSwitchState(boolean isOn)
    {
        notificationSwitch.setChecked(isOn);
    }
}

//TODO: Figure out why the notification toggle stays on on restart