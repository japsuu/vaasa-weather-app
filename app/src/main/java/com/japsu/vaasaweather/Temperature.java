package com.japsu.vaasaweather;

import android.app.AlarmManager;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Temperature
{
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