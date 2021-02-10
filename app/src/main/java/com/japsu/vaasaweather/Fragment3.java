package com.japsu.vaasaweather;

import android.os.AsyncTask;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Fragment3 extends Fragment
{
    static TextView curLevelView = null;
    static TextView maxLevelView = null;
    static TextView minLevelView = null;
    static Button updateBtn = null;
    static ProgressBar updateProgress = null;
    static GraphView graph = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment3_layout, container, false);
        curLevelView = (TextView) view.findViewById(R.id.curSealevel);
        maxLevelView = (TextView) view.findViewById(R.id.maxSealevel);
        minLevelView = (TextView) view.findViewById(R.id.minSealevel);
        graph = (GraphView) view.findViewById(R.id.sealGraph);
        updateBtn = view.findViewById(R.id.sealUpdateBtn);
        updateProgress = view.findViewById(R.id.sealUpdateProgress);
        updateBtn.setOnClickListener(v -> GetLevel());

        GetLevel();

        return  view;
    }

    private static void BuildGraph(Double[] values)
    {
        DataPoint[] points = new DataPoint[values.length];
        for (int i = 0; i < values.length; i++)
        {
            points[i] = new DataPoint(i, values[i]);
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);

        graph.addSeries(series);
    }

    private static void GetLevel()
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
        String highest = "Korkein saatavilla: ";
        String lowest = "Matalin saatavilla: ";

        //check if the text object exists, to circumvent any null reference exceptions
        if(curLevelView != null && maxLevelView != null && minLevelView != null)
        {
            Log.d("SEAL", "Meren korkeusdata vastaanotettu!");
            new Handler().postDelayed(() -> HideProgress(), 100);

            Double[] levels = new Double[result.length];
            for (int i = 0; i < result.length; i++)
            {
                levels[i] = result[i].value;
            }
            BuildGraph(levels);

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

                current += levels[levels.length - 1] + "cm";
                highest += maxValue.toString() + "cm";
                lowest += minValue.toString() + "cm";
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
            new DownloadSealevel().execute(new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::forecast::oaas::sealevel::point::timevaluepair&latlon=63.08,21.57"));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}

class SealevelData
{
    Double value;
    String date;

    SealevelData(Double value, String date)
    {
        this.value = value;
        this.date = date;
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

        //try to download the document
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(urls[0].toString()).openStream());
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

        //get the actual listings from the document
        NodeList results = doc.getElementsByTagName("wml2:MeasurementTVP");
        SealevelData[] result = new SealevelData[results.getLength()];

        for (int i = 0; i < results.getLength(); i++)
        {
            String date = results.item(i).getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
            Double value = Double.parseDouble(date.substring(date.indexOf("Z") + 1));

            date = date.substring(0, date.indexOf("Z") - 1).replace("T", " ");
            result[i] = new SealevelData(value, date);

            Log.d("SEAL", result[i].value.toString());
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