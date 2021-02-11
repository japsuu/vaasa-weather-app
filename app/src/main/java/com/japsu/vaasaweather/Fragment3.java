package com.japsu.vaasaweather;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    static Button updateBtn = null;
    static ImageView infoBtn = null;
    static ProgressBar updateProgress = null;
    static GraphView graph = null;
    static Context context = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment3_layout, container, false);

        context = getContext();
        curLevelView = (TextView) view.findViewById(R.id.curSealevel);
        maxLevelView = (TextView) view.findViewById(R.id.maxSealevel);
        minLevelView = (TextView) view.findViewById(R.id.minSealevel);
        graph = (GraphView) view.findViewById(R.id.sealGraph);
        updateBtn = view.findViewById(R.id.sealUpdateBtn);
        infoBtn = view.findViewById(R.id.sealInfoBtn);
        updateProgress = view.findViewById(R.id.sealUpdateProgress);
        updateBtn.setOnClickListener(v -> GetLevel());
        infoBtn.setOnClickListener(v -> ShowInfo());

        return  view;
    }

    private static void BuildGraph(SealevelData[] values)
    {
        Double[] levels = new Double[values.length];
        Double[] temps = new Double[values.length];
        String[] dates = new String[values.length];

        for (int i = 0; i < values.length; i++)
        {
            levels[i] = values[i].level;
            temps[i] = values[i].temp;
            dates[i] = values[i].dateUnformatted;
        }

        graph.removeAllSeries();

        DataPoint[] levelPoints = new DataPoint[values.length];
        DataPoint[] tempPoints = new DataPoint[temps.length];

        for (int i = 0; i < values.length; i++)
        {
            levelPoints[i] = new DataPoint(i, levels[i]);
            tempPoints[i] = new DataPoint(i, temps[i]);
        }

        LineGraphSeries<DataPoint> levelSeries = new LineGraphSeries<>(levelPoints);
        BarGraphSeries<DataPoint> tempSeries = new BarGraphSeries<>(tempPoints);

        levelSeries.setColor(Color.BLUE);
        tempSeries.setColor(Color.RED);

        levelSeries.setDrawDataPoints(true);
        levelSeries.setDataPointsRadius(6);

        levelSeries.setTitle("Korkeus");
        tempSeries.setTitle("Lämpötila");

        graph.setTitle("Päivitetty: " + values[values.length - 1].time);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Mittauspäivä");
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(25);
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getGridLabelRenderer().setNumVerticalLabels(7);

        levelSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");
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

                Toast.makeText(context, "" + dataPoint.getY() + " aikana " + formattedTime, Toast.LENGTH_SHORT).show();
            }
        });

        //TODO: set manual bounds and enable scrolling

        // custom label formatter to show labels as "mm"
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX)
                {
                    Integer val = (int)value;
                    String date = dates[val];

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");
                    Date d = null;

                    try
                    {
                        d = sdf.parse(date);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    String formattedTime = output.format(d);

                    return formattedTime;
                }
                else
                {
                    return super.formatLabel(value, isValueX) + "mm";
                }
            }
        });

        graph.addSeries(levelSeries);
        graph.addSeries(tempSeries);
    }

    private void ShowInfo()
    {
        Toast.makeText(this.getActivity(), "Ilmatieteenlaitos päivittää dataa vain noin kahden tunnin välein!", Toast.LENGTH_LONG).show();
    }

    public static void GetLevel()
    {
        GetSealevel();
        if(updateProgress != null)
        {
            ShowProgress();

            //new Handler().postDelayed(() -> HideProgress(), 600);
        }
        else
        {
            Log.d("SEAL", "Progressbar is null");
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
        String current = "Tämänhetkinen: ";
        String highest = "Viikon korkein: ";
        String lowest = "Viikon matalin: ";

        //check if the text object exists, to circumvent any null reference exceptions
        if(curLevelView != null && maxLevelView != null && minLevelView != null)
        {
            BuildGraph(result);

            Log.d("SEAL", "Marine data received! Data sample size: " + result.length);
            new Handler().postDelayed(Fragment3::HideProgress, 100);

            Double[] levels = new Double[result.length];
            for (int i = 0; i < result.length; i++)
            {
                levels[i] = result[i].level;
            }

            //check if the data contains anything
            if(levels.length != 0)
            {
                //get the highest and lowest values in the array
                Double maxValue = -9999.9999;
                Double minValue = 9999.9999;
                for(int i = 0; i < levels.length; i++)
                {
                    if(levels[i] > maxValue)
                    {
                        maxValue = levels[i];
                    }
                    if(levels[i] < minValue)
                    {
                        minValue = levels[i];
                    }
                }

                current += levels[levels.length - 1] + "mm";
                highest += maxValue.toString() + "mm";
                lowest += minValue.toString() + "mm";
            }
            else
            {
                current += "Ei Korkeusdataa... :(";
                highest += "Ei Korkeusdataa... :(";
                lowest += "Ei Korkeusdataa... :(";
            }

            //convert the strings to Spanned for HTML support, and set the textviews' texts:
            //current
            Spanned spannedCurrent = Html.fromHtml(current);
            curLevelView.setText(spannedCurrent);
            //max
            Spanned spannedMax = Html.fromHtml(highest);
            maxLevelView.setText(spannedMax);
            //min
            Spanned spannedMin = Html.fromHtml(lowest);
            minLevelView.setText(spannedMin);
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

    public String getAsString()
    {
        return time + "- Level: " + level.toString() + " Temp: " + temp.toString();
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
                                Double level = Double.parseDouble(levelNodes.item(i).getChildNodes().item(j).getChildNodes().item(k + 2).getTextContent());
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