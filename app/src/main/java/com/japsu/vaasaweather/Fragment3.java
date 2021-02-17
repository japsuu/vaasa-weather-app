package com.japsu.vaasaweather;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Fragment3 extends Fragment
{
    static TextView curLevelView = null;
    static TextView maxLevelView = null;
    static TextView minLevelView = null;
    static TextView curTempView = null;
    static TextView maxTempView = null;
    static TextView minTempView = null;
    static Button updateBtn = null;
    static ImageView infoBtn = null;
    static ProgressBar updateProgress = null;
    static GraphView graphSeal = null;
    static GraphView graphSeat = null;
    static Context context = null;
    static CustomScrollView seaScrollView = null;

    static Double maxSealValue = -9999.9999;
    static Double minSealValue = 9999.9999;
    static Double maxTempValue = -9999.9999;
    static Double minTempValue = 9999.9999;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment3_layout, container, false);

        context = getContext();
        curLevelView = (TextView) view.findViewById(R.id.curSealevel);
        maxLevelView = (TextView) view.findViewById(R.id.maxSealevel);
        minLevelView = (TextView) view.findViewById(R.id.minSealevel);
        curTempView = (TextView) view.findViewById(R.id.curSeatemp);
        maxTempView = (TextView) view.findViewById(R.id.maxSeatemp);
        minTempView = (TextView) view.findViewById(R.id.minSeatemp);
        graphSeal = (GraphView) view.findViewById(R.id.sealGraph);
        graphSeat = (GraphView) view.findViewById(R.id.seatGraph);
        seaScrollView = (CustomScrollView) view.findViewById(R.id.seaScrollView);
        updateBtn = view.findViewById(R.id.sealUpdateBtn);
        infoBtn = view.findViewById(R.id.sealInfoBtn);
        updateProgress = view.findViewById(R.id.sealUpdateProgress);
        updateBtn.setOnClickListener(v -> GetLevel());
        infoBtn.setOnClickListener(v -> ShowInfo());
        graphSeal.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = MotionEventCompat.getActionMasked(event);
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.viewPager.setEnableSwipe(false);
                        seaScrollView.setEnableScrolling(false);
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        MainActivity.viewPager.setEnableSwipe(true);
                        seaScrollView.setEnableScrolling(true);
                        return false;
                    default:
                        return false;
                }
            }
        });
        graphSeat.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = MotionEventCompat.getActionMasked(event); // FIXME: 14.2.2021
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        MainActivity.viewPager.setEnableSwipe(false);
                        seaScrollView.setEnableScrolling(false);
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        MainActivity.viewPager.setEnableSwipe(true);
                        seaScrollView.setEnableScrolling(true);
                        return false;
                    default:
                        return false;
                }
            }
        });

        return  view;
    }

    private static void BuildGraph(SealevelData[] values)
    {
        graphSeal.removeAllSeries();
        graphSeat.removeAllSeries();

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true);
        @ColorInt int colorOnBackground = typedValue.data;

        Double[] levels = new Double[values.length];
        Double[] temps = new Double[values.length];
        String[] dates = new String[values.length];
        String[] times = new String[values.length];

        for (int i = 0; i < values.length; i++)
        {
            levels[i] = values[i].level;
            temps[i] = values[i].temp;
            dates[i] = values[i].dateUnformatted;
            times[i] = values[i].time;
        }

        DataPoint[] levelPoints = new DataPoint[values.length];
        DataPoint[] tempPoints = new DataPoint[temps.length];

        for (int i = 0; i < values.length; i++)
        {
            levelPoints[i] = new DataPoint(i, levels[i]);
            tempPoints[i] = new DataPoint(i, temps[i]);
        }

        LineGraphSeries<DataPoint> levelSeries = new LineGraphSeries<>(levelPoints);
        LineGraphSeries<DataPoint> tempSeries = new LineGraphSeries<>(tempPoints);


        graphSeal.setTitle("Päivitetty: " + times[times.length - 1]);
        graphSeal.getViewport().setXAxisBoundsManual(true);
        graphSeal.getViewport().setMaxX(values.length);
        graphSeal.getViewport().setMinX(0);
        graphSeal.getViewport().setYAxisBoundsManual(true);
        graphSeal.getViewport().setMaxY(maxSealValue + 30);
        graphSeal.getViewport().setMinY(maxSealValue - 30);
        graphSeal.getViewport().setScalable(true);
        graphSeal.getLegendRenderer().setVisible(true);
        graphSeal.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphSeal.getGridLabelRenderer().setNumHorizontalLabels(5);
        graphSeat.getGridLabelRenderer().setNumVerticalLabels(7);
        graphSeal.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graphSeal.getGridLabelRenderer().setVerticalLabelsColor(colorOnBackground);
        graphSeal.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX)
                {
                    if((int)value == times.length)
                    {
                        return null;
                    }
                    else
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        SimpleDateFormat output = new SimpleDateFormat("HH:mm");
                        Date t = null;

                        try
                        {
                            t = sdf.parse(times[(int)value]);
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                        assert t != null;
                        return output.format(t);
                    }
                }
                else
                {
                    // show temp for y values
                    return super.formatLabel(value, isValueX) + "cm";
                }
            }
        });

        levelSeries.setTitle("Korkeus");
        levelSeries.setColor(colorOnBackground);
        levelSeries.setThickness(2);
        levelSeries.setDrawDataPoints(true);
        levelSeries.setDataPointsRadius(4);
        levelSeries.setOnDataPointTapListener((series, dataPoint) ->
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


        graphSeal.addSeries(levelSeries);


        graphSeat.setTitle("Päivitetty: " + times[times.length - 1]);
        graphSeat.getViewport().setXAxisBoundsManual(true);
        graphSeat.getViewport().setMaxX(values.length);
        graphSeat.getViewport().setMinX(0);
        graphSeat.getViewport().setYAxisBoundsManual(true);
        graphSeat.getViewport().setMaxY(maxTempValue + 3);
        graphSeat.getViewport().setMinY(maxTempValue - 3);
        graphSeat.getViewport().setScalable(true);
        graphSeat.getLegendRenderer().setVisible(true);
        graphSeat.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphSeat.getGridLabelRenderer().setNumHorizontalLabels(5);
        graphSeat.getGridLabelRenderer().setNumVerticalLabels(7);
        graphSeat.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graphSeat.getGridLabelRenderer().setVerticalLabelsColor(Color.RED);
        graphSeat.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX)
                {
                    if((int)value == times.length)
                    {
                        return null;
                    }
                    else
                    {
                        return times[(int)value];
                    }
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


        graphSeat.addSeries(tempSeries);


        /*

        levelSeries.setTitle("Korkeus");
        levelSeries.setColor(colorOnBackground);
        levelSeries.setDrawDataPoints(true);
        levelSeries.setDataPointsRadius(4);
        levelSeries.setOnDataPointTapListener((series, dataPoint) ->
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

        tempSeries.setTitle("Lämpötila");
        tempSeries.setColor(Color.RED);
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

        graphSeal.addSeries(levelSeries);
        graphSeat.addSeries(tempSeries);

        graphSeal.setTitle("Päivitetty: " + values[values.length - 1].time);
        graphSeat.setTitle("Päivitetty: " + values[values.length - 1].time);

        graphSeal.getLegendRenderer().setVisible(true);
        graphSeal.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphSeat.getLegendRenderer().setVisible(true);
        graphSeat.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graphSeal.getViewport().setXAxisBoundsManual(true);
        graphSeal.getViewport().setYAxisBoundsManual(true);
        graphSeal.getViewport().setMaxX(values[values.length - 1].level);
        graphSeal.getViewport().setMinX(values[0].level);
        graphSeal.getViewport().setMaxY(maxSealValue + 50);
        graphSeal.getViewport().setMinY(minSealValue - 50);
        graphSeal.getViewport().setScalable(true);
        graphSeal.getGridLabelRenderer().setNumHorizontalLabels(5);
        graphSeal.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graphSeal.getGridLabelRenderer().setVerticalLabelsColor(colorOnBackground); // FIXME: 14.2.2021
        graphSeal.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
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
                    return super.formatLabel(value, isValueX) + "cm";
                }
            }
        });

        graphSeat.getViewport().setXAxisBoundsManual(true);
        graphSeat.getViewport().setYAxisBoundsManual(true);
        graphSeat.getViewport().setMaxX(values.length);
        graphSeat.getViewport().setMinX(0);
        graphSeat.getViewport().setMaxY(maxTempValue + 2);
        graphSeat.getViewport().setMinY(minTempValue - 2);
        graphSeat.getViewport().setScalable(true);
        graphSeat.getGridLabelRenderer().setNumHorizontalLabels(5);
        graphSeat.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graphSeat.getGridLabelRenderer().setVerticalLabelsColor(colorOnBackground); // FIXME: 14.2.2021
        graphSeat.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
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
                    return super.formatLabel(value, isValueX) + "\u2103";
                }
            }
        });*/
    }

    private void ShowInfo()
    {
        Toast.makeText(this.getActivity(), "Ilmatieteenlaitos päivittää dataa vain muutamien tuntien välein! Lue lisää 'info' välilehdeltä.", Toast.LENGTH_LONG).show();
    }

    public static void GetLevel()
    {
        GetSealevel();
        if(updateProgress != null)
        {
            ShowProgress();

            //new Handler().postDelayed(() -> HideProgress(), 600);
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
        if(curLevelView != null && maxLevelView != null && minLevelView != null)
        {
            curLevelView.setText(value);
            maxLevelView.setText(value);
            minLevelView.setText(value);
        }
    }

    // FIXME: 10.2.2021
    public static void UpdateTextfields(SealevelData[] result)
    {
        String currentSeal = "Tämänhetkinen: ";
        String highestSeal = "Viiden päivän korkein: ";
        String lowestSeal = "Viiden päivän matalin: ";
        String currentTemp = "Tämänhetkinen: ";
        String highestTemp = "Viiden päivän korkein: ";
        String lowestTemp = "Viiden päivän matalin: ";

        //check if the text object exists, to circumvent any null reference exceptions
        if(curLevelView != null && maxLevelView != null && minLevelView != null)
        {
            Log.d("SEAL", "Marine data received! Data sample size: " + result.length);
            new Handler().postDelayed(Fragment3::HideProgress, 100);

            Double[] levels = new Double[result.length];
            Double[] temps = new Double[result.length];

            for (int i = 0; i < result.length; i++)
            {
                levels[i] = result[i].level;
                temps[i] = result[i].temp;
            }

            //check if the data contains anything
            if(levels.length != 0)
            {
                //get the highest and lowest values in the array

                for (Double level : levels)
                {
                    if (level > maxSealValue)
                    {
                        maxSealValue = level;
                    }
                    if (level < minSealValue)
                    {
                        minSealValue = level;
                    }
                }

                for (Double temp : temps)
                {
                    if (temp > maxTempValue)
                    {
                        maxTempValue = temp;
                    }
                    if (temp < minTempValue)
                    {
                        minTempValue = temp;
                    }
                }

                currentSeal += levels[levels.length - 1] + "cm";
                highestSeal += maxSealValue.toString() + "cm";
                lowestSeal += minSealValue.toString() + "cm";

                currentTemp += temps[temps.length - 1] + "\u2103";
                highestTemp += maxTempValue.toString() + "\u2103";
                lowestTemp += minTempValue.toString() + "\u2103";
            }
            else
            {
                currentSeal += "Ei Korkeusdataa... :(";
                highestSeal += "Ei Korkeusdataa... :(";
                lowestSeal += "Ei Korkeusdataa... :(";
                currentTemp += "Ei Lämpötiladataa... :(";
                highestTemp += "Ei Lämpötiladataa... :(";
                lowestTemp += "Ei Lämpötiladataa... :(";
            }

            //convert the strings to Spanned for HTML support, and set the textviews' texts:
            //current
            Spanned spannedCurrentSeal = Html.fromHtml(currentSeal);
            curLevelView.setText(spannedCurrentSeal);
            Spanned spannedCurrentTemp = Html.fromHtml(currentTemp);
            curTempView.setText(spannedCurrentTemp);
            //max
            Spanned spannedMaxSeal = Html.fromHtml(highestSeal);
            maxLevelView.setText(spannedMaxSeal);
            Spanned spannedMaxTemp = Html.fromHtml(highestTemp);
            maxTempView.setText(spannedMaxTemp);
            //min
            Spanned spannedMinSeal = Html.fromHtml(lowestSeal);
            minLevelView.setText(spannedMinSeal);
            Spanned spannedMinTemp = Html.fromHtml(lowestTemp);
            minTempView.setText(spannedMinTemp);

            //we build the graph of the result
            BuildGraph(result);
        }
    }

    public static void GetSealevel()
    {
        try
        {
            //start a new async event for downloading the document
            new DownloadSealevel().execute(new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::observations::mareograph::timevaluepair&fmisid=134223&starttime="));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}

class SealevelData
{
    String time;
    String dateUnformatted;
    Double level;
    Double temp;

    SealevelData(String time, String dateUnformatted, Double level, Double temp)
    {
        this.time = time;
        this.level = level;
        this.temp = temp;
        this.dateUnformatted = dateUnformatted;
    }
}

    //TODO: change the texts from today's highest etc to highest available and date
class DownloadSealevel extends AsyncTask<URL, Integer, SealevelData[]>
{
    @Override
    protected SealevelData[] doInBackground(URL... urls)
    {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        //get the current dates and times
        Date startTime = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5));
        SimpleDateFormat startTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


        Log.d("SEAL", "Downloading marine data from: " + urls[0] + startTimeFormatter.format(startTime));

        //try to download the document
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL((urls[0] + startTimeFormatter.format(startTime))).openStream());
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

        NodeList root = doc.getElementsByTagName("wml2:MeasurementTimeseries");
        NodeList levelNodes = root.item(0).getChildNodes();
        NodeList tempNodes = root.item(1).getChildNodes();

        SealevelData[] result = new SealevelData[(levelNodes.getLength() - 1) / 2];

        for (int i = 0; i < levelNodes.getLength(); i++)
        {
            if(levelNodes.item(i).getNodeName().contains("wml2:point"))
            {
                for (int j = 0; j < levelNodes.item(i).getChildNodes().getLength(); j++)
                {
                    if(levelNodes.item(i).getChildNodes().item(j).getNodeName().contains("MeasurementTVP"))
                    {
                        for (int k = 0; k < levelNodes.item(i).getChildNodes().item(j).getChildNodes().getLength(); k++)
                        {
                            if(levelNodes.item(i).getChildNodes().item(j).getChildNodes().item(k).getNodeName().contains("time"))
                            {
                                String unformattedTime = levelNodes.item(i).getChildNodes().item(j).getChildNodes().item(k).getTextContent();
                                String time = unformattedTime.substring(unformattedTime.indexOf("T") + 1).replace("Z", "");
                                Double level = Double.parseDouble(levelNodes.item(i).getChildNodes().item(j).getChildNodes().item(k + 2).getTextContent()) / 10;
                                Double temp = Double.parseDouble(tempNodes.item(i).getChildNodes().item(j).getChildNodes().item(k + 2).getTextContent());
                                SealevelData data = new SealevelData(time, unformattedTime, level, temp);
                                result[(i - 1) / 2] = data;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(SealevelData[] result)
    {
        //if the result contains something, we pass it to the handling method
        if(result != null)
        {
            Fragment3.UpdateTextfields(result);
        }
        else
        {
            Fragment3.UpdateTextfields("Ei internetyhteyttä");
        }
    }
}