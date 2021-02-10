package com.japsu.vaasaweather;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Fragment2 extends Fragment
{
    static TextView curTempView = null;
    static TextView maxTempView = null;
    static TextView minTempView = null;
    static Button updateBtn = null;
    static ProgressBar updateProgress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment2_layout, container, false);
        curTempView = (TextView) view.findViewById(R.id.curTemperature);
        maxTempView = (TextView) view.findViewById(R.id.maxTemperature);
        minTempView = (TextView) view.findViewById(R.id.minTemperature);
        updateBtn = view.findViewById(R.id.tempUpdateBtn);
        updateProgress = view.findViewById(R.id.tempUpdateProgress);
        updateBtn.setOnClickListener(v -> GetTemp());

        GetTemp();
        return view;
    }

    private static void GetTemp()   //TODO: Also make another weather widget for the full week weather
    {
        GetTemperature(false);
        if(updateProgress != null)
        {
            ShowProgress();

            //new Handler().postDelayed(() -> HideProgress(), 600);
        }
        else
        {
            Log.d("TEMP", "Progressbar is null");
        }
    }

    private static void ShowProgress()
    {
        updateProgress.setVisibility(View.VISIBLE);
    }

    private static void HideProgress()
    {
        updateProgress.setVisibility(View.INVISIBLE);
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
            Log.d("TEMP", "Lämpötila data vastaanotettu!");
            new Handler().postDelayed(() -> HideProgress(), 100);

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

    /**
     *  ALL THE TEMPERATURE DOWNLOAD RELATED STUFF LIES DOWN HERE
     */

    public static void GetTemperature(boolean isBackgroundTask)
    {
        try
        {
            //start a new async event for downloading the document
            new DownloadTemperature().execute(new TaskParams(new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::observations::weather::multipointcoverage&place=vaasa&starttime=STARTTIMEHERET00:00:00Z&endtime=CURRENTDATEHERETCURRENTTIMEHEREZ&Parameters=temperature"), isBackgroundTask));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}

class DownloadTemperature extends AsyncTask<TaskParams, Integer, Double[]>
{
    @Override
    protected Double[] doInBackground(TaskParams... params)
    {
        String str = "";
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        //get the current dates and times
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        //try to download the document
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(params[0].url.toString().replace("STARTTIMEHERE", dateFormatter.format(date)).replace("CURRENTDATEHERETCURRENTTIMEHERE", timeFormatter.format(date).replace("_", "T"))).openStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //we return early if there was a unknown problem downloading the document, to prevent crashes
        if(doc == null)
        {
            return null;
        }

        //get the actual temp elements from the document, and make it more readable
        NodeList results = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList");
        String[] temps;
        str = results.item(0).getTextContent().replace(" ", "");
        str = str.replace("\n", ";");
        str = str.substring(1);
        str = str.substring(0, str.length() - 1);
        temps = str.split(";");

        //if it's a background task (called for a notification, we have no UI available)
        if(params[0].state)
        {
            double minValue = 99.99;
            for(int i = 0; i < temps.length; i++)
            {
                if(Double.parseDouble(temps[i]) < minValue)
                {
                    minValue = Double.parseDouble(temps[i]);
                }
            }

            Double[] result = new Double[1];
            result[0] = minValue;

            //return the double array to onPostExecute
            return result;
        }
        else
        {
            //convert the resulting string array to a double array
            Double[] result = new Double[temps.length];
            for(int i = 0; i < temps.length; i++)
            {
                result[i] = Double.parseDouble(temps[i]);
            }
            //return the double array to onPostExecute
            return result;
        }
    }

    @Override
    protected void onPostExecute(Double[] result)
    {
        //if the result contains something, we pass it to the corresponding "page's" (fragment's) handling method, or if the size is only 1 element we know it's a background update 4 a notification!
        if(result != null)
        {
            if(result.length == 1)
            {
                AlarmReceiver.deliverNotification(AlarmReceiver.cntxt, result[0]);
            }
            else
            {
                Fragment2.UpdateTextfields(result);
            }
        }
        else
        {
            Fragment2.UpdateTextfields("Ei internetyhteyttä");
        }
    }
}