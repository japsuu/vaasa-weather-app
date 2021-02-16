package com.japsu.vaasaweather;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.Currency;
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
    static GraphView graph = null;
    static ImageView infoBtn = null;
    static Context context = null;
    static CustomScrollView tempScrollView = null;

    static Double maxTempValue = -9999.9999;
    static Double minTempValue = 9999.9999;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment2_layout, container, false);
        context = getContext();
        curTempView = (TextView) view.findViewById(R.id.curTemperature);
        maxTempView = (TextView) view.findViewById(R.id.maxTemperature);
        minTempView = (TextView) view.findViewById(R.id.minTemperature);
        tempScrollView = (CustomScrollView) view.findViewById(R.id.tempScrollView);
        infoBtn = view.findViewById(R.id.tempInfoBtn);
        graph = (GraphView) view.findViewById(R.id.tempGraph);
        updateBtn = view.findViewById(R.id.tempUpdateBtn);
        updateProgress = view.findViewById(R.id.tempUpdateProgress);
        updateBtn.setOnClickListener(v -> GetTemp());
        infoBtn.setOnClickListener(v -> ShowInfo());
        graph.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = MotionEventCompat.getActionMasked(event);  // FIXME: 14.2.2021
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.viewPager.setEnableSwipe(false);
                        tempScrollView.setEnableScrolling(false);
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        MainActivity.viewPager.setEnableSwipe(true);
                        tempScrollView.setEnableScrolling(true);
                        return false;
                    default:
                        return false;
                }
            }
        });

        return view;
    }

    private static void BuildGraph(TempData[] values)
    {
        graph.removeAllSeries();


        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true);
        @ColorInt int colorOnBackground = typedValue.data;

        //extract items from the data
        Double[] temps = new Double[values.length];
        String[] dates = new String[values.length];
        String[] times = new String[values.length];

        for (int i = 0; i < values.length; i++)
        {
            temps[i] = values[i].temp;
            dates[i] = values[i].dateUnformatted;
            times[i] = values[i].time;
        }

        DataPoint[] tempPoints = new DataPoint[values.length];
        for (int i = 0; i < values.length; i++) // FIXME: 14.2.2021 some problems could be resolved by changing the x value..?
        {
            tempPoints[i] = new DataPoint(i, temps[i]);
        }
        LineGraphSeries<DataPoint> tempSeries = new LineGraphSeries<>(tempPoints);


        graph.setTitle("Päivitetty: " + times[times.length - 1]);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(values.length);
        graph.getViewport().setMinX(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(maxTempValue + 3);
        graph.getViewport().setMinY(minTempValue - 3);
        graph.getViewport().setScalable(true);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getGridLabelRenderer().setNumVerticalLabels(7);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graph.getGridLabelRenderer().setVerticalLabelsColor(colorOnBackground);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX)
                {
                    return times[(int)value];
                }
                else
                {
                    // show temp for y values
                    return super.formatLabel(value, isValueX) + "\u2103";
                }
            }
        });

        tempSeries.setTitle("Lämpötila");
        tempSeries.setColor(Color.RED);
        tempSeries.setThickness(2);
        tempSeries.setDrawDataPoints(true);
        tempSeries.setDataPointsRadius(4);
        tempSeries.setOnDataPointTapListener((series, dataPoint) ->
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
            Date d = null;

            try
            {
                d = sdf.parse(dates[(int)dataPoint.getX()]);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }

            String formattedTime = output.format(d);

            Toast.makeText(context, "" + dataPoint.getY() + " kello " + formattedTime, Toast.LENGTH_SHORT).show();
        });


        graph.addSeries(tempSeries);
    }

    private void ResetGraphZoom()
    {
        graph.getViewport().scrollToEnd();
    }

    private void ShowInfo()
    {
        Toast.makeText(this.getActivity(), "Ilmatieteenlaitos päivittää dataa vain muutamien tuntien välein!", Toast.LENGTH_LONG).show();
    }

    public static void GetTemp()   //TODO: Also make another weather widget for the full week weather
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

    public static void UpdateTextfields(TempData[] result)
    {
        String current = "Tämänhetkinen: ";
        String highest = "Päivän korkein: ";
        String lowest = "Päivän matalin: ";

        //check if the text object exists, to circumvent any null reference exceptions
        if(curTempView != null && maxTempView != null && minTempView != null)
        {
            Log.d("TEMP", "Temperature data received");
            new Handler().postDelayed(Fragment2::HideProgress, 100);

            //check if the data contains anything
            if(result.length != 0)
            {
                //get the highest and lowest values in the array
                for(int i = 0; i < result.length; i++)
                {
                    if(result[i].temp > maxTempValue)
                    {
                        maxTempValue = result[i].temp;
                    }
                    if(result[i].temp < minTempValue)
                    {
                        minTempValue = result[i].temp;
                    }
                }

                current += result[result.length - 1].temp + "\u2103";
                highest += maxTempValue.toString() + "\u2103";
                lowest += minTempValue.toString() + "\u2103";
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

        //we build the graph of the result
        BuildGraph(result);
    }

    /**
     *  ALL THE TEMPERATURE DOWNLOAD RELATED STUFF LIES DOWN HERE
     */

    public static void GetTemperature(boolean isBackgroundTask)
    {
        try
        {
            //start a new async event for downloading the document
            new DownloadTemperature().execute(new TaskParams(new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::observations::weather::timevaluepair&place=vaasa&starttime=STARTTIMEHERET00:00:00Z&Parameters=temperature"), isBackgroundTask));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}

class TempData
{
    String dateUnformatted;
    String time;
    Double temp;

    TempData(String dateUnformatted, String time, Double temp)
    {
        this.temp = temp;
        this.dateUnformatted = dateUnformatted;
        this.time = time;
    }
}

class DownloadTemperature extends AsyncTask<TaskParams, Integer, TempData[]>
{
    @Override
    protected TempData[] doInBackground(TaskParams... params)
    {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        //get the current dates and times
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        Log.d("TEMP", "Downloading temperature data from: " + params[0].url.toString().replace("STARTTIMEHERE", dateFormatter.format(date)));

        //try to download the document
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(params[0].url.toString().replace("STARTTIMEHERE", dateFormatter.format(date))).openStream());
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

        /*
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

            TempData[] result = new TempData[1];
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
        }*/

        NodeList root = doc.getElementsByTagName("wml2:MeasurementTimeseries");
        NodeList tempNodes = root.item(0).getChildNodes();

        TempData[] result = new TempData[(tempNodes.getLength() - 1) / 2];

        for (int i = 0; i < tempNodes.getLength(); i++)
        {
            if(tempNodes.item(i).getNodeName().contains("wml2:point"))
            {
                for (int j = 0; j < tempNodes.item(i).getChildNodes().getLength(); j++)
                {
                    if(tempNodes.item(i).getChildNodes().item(j).getNodeName().contains("MeasurementTVP"))
                    {
                        for (int k = 0; k < tempNodes.item(i).getChildNodes().item(j).getChildNodes().getLength(); k++)
                        {
                            if(tempNodes.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().contains("time"))
                            {
                                String unformattedTime = tempNodes.item(i).getChildNodes().item(j).getChildNodes().item(k).getTextContent();
                                String time = unformattedTime.substring(unformattedTime.indexOf("T") + 1).replace("Z", "");
                                Double temp = Double.parseDouble(tempNodes.item(i).getChildNodes().item(j).getChildNodes().item(k + 2).getTextContent());
                                TempData data = new TempData(unformattedTime, time, temp);
                                result[(i - 1) / 2] = data;
                                // FIXME: 14.2.2021
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(TempData[] result)
    {
        //if the result contains something, we pass it to the corresponding "page's" (fragment's) handling method, or if the size is only 1 element we know it's a background update 4 a notification!
        if(result != null)
        {
            if(result.length == 1)
            {
                AlarmReceiver.deliverNotification(AlarmReceiver.cntxt, result[0].temp);
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