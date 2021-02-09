package com.japsu.vaasaweather;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Collections;
import java.util.List;

public class Fragment2 extends Fragment
{
    static TextView curTempView = null;
    static TextView maxTempView = null;
    static TextView minTempView = null;
    static Button updateBtn = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Temperature.GetTemperature();
        View view = inflater.inflate(R.layout.fragment2_layout, container, false);
        curTempView = (TextView) view.findViewById(R.id.curTemperature);
        maxTempView = (TextView) view.findViewById(R.id.maxTemperature);
        minTempView = (TextView) view.findViewById(R.id.minTemperature);
        updateBtn = view.findViewById(R.id.tempUpdateBtn);
        updateBtn.setOnClickListener(v -> Temperature.GetTemperature());

        return view;
    }

    public static void UpdateTextfields(String value)
    {
        if(curTempView != null && maxTempView != null && minTempView != null)
        {
            curTempView.setText(value);
            maxTempView.setText(value);
            minTempView.setText(value);
        }
    }

    public static void UpdateTextfields(Double[] result)
    {
        String current = "Tämänhetkinen: ";
        String highest = "Päivän korkein: ";
        String lowest = "Päivän matalin: ";

        //check if the text object exists, to circumvent any null reference exceptions
        if(curTempView != null && maxTempView != null && minTempView != null)
        {
            Log.d("TEMP", "Lämpötila data vastaanotettu:\n");

            //check if the data contains anything
            if(result.length != 0)
            {
                //get the highest and lowest values in the array
                Double maxValue = -99.99;
                Double minValue = 99.99;
                for(int i = 0; i < result.length; i++)
                {
                    if(result[i] > maxValue)
                    {
                        maxValue = result[i];
                    }
                    if(result[i] < minValue)
                    {
                        minValue = result[i];
                    }
                }

                current += result[result.length - 1] + "\u2103";
                highest += maxValue.toString() + "\u2103";
                lowest += minValue.toString() + "\u2103";
            }
            else
            {
                current += "Ei Lämpötiladataa... :(";
                highest += "Ei Lämpötiladataa... :(";
                lowest += "Ei Lämpötiladataa... :(";
            }

            //convert the strings to Spanned for HTML support, and set the textviews' texts:
            //current
            Spanned spannedCurrent = Html.fromHtml(current);
            curTempView.setText(spannedCurrent);
            //max
            Spanned spannedMax = Html.fromHtml(highest);
            maxTempView.setText(spannedMax);
            //min
            Spanned spannedMin = Html.fromHtml(lowest);
            minTempView.setText(spannedMin);
        }
    }
}
