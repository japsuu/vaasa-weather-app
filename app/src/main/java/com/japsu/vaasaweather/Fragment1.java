package com.japsu.vaasaweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Fragment1 extends Fragment
{
    static TextView warningsView = null;
    static Switch warningSwitch = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment1_layout, container, false);
        warningsView = (TextView) view.findViewById(R.id.warningContents);
        warningsView.setMovementMethod(new ScrollingMovementMethod());
        warningSwitch = (Switch) view.findViewById(R.id.warningSwitch);
        GetWarnings(warningSwitch.isChecked());
        warningSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            Log.d("TESTING", "Switch state changed");
            GetWarnings(isChecked);
        });

        return view;
    }

    public static void UpdateTextfield(String progress)
    {
        if(warningsView != null)
        {
            warningsView.setText(progress);
        }
    }

    public static void UpdateTextfield(List<String> result)
    {
        String warnings = "";
        if(warningsView != null)
        {
            Log.d("WARN", "Varoitusten data vastaanotettu:\n");
            if(result.size() != 1)
            {
                for (int i = 0; i < result.size(); i++)
                {
                    warnings += result.get(i);
                }
            }
            else
            {
                warnings += "Ei aktiivisia varoituksia.";
            }
            Spanned spannedResult = Html.fromHtml(warnings);
            warningsView.setText(spannedResult);
        }
    }

    public static void GetWarnings(boolean switchState)
    {
        try
        {
            TaskParams pars = new TaskParams(new URL("https://alerts.fmi.fi/cap/feed/rss_fi-FI.rss"), switchState);
            new DownloadWarnings().execute(pars);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  ALL THE STUFF RELATED TO DOWNLOADING WARNINGS LIES DOWN HERE
     */
}

class TaskParams
{
    URL url;
    boolean state;

    TaskParams(URL url, boolean isChecked)
    {
        this.url = url;
        this.state = isChecked;
    }
}

class DownloadWarnings extends AsyncTask<TaskParams, Integer, List<String>>
{
    @Override
    protected List<String> doInBackground(TaskParams... params)
    {
        URL urls = params[0].url;
        boolean isChecked = params[0].state;

        List<String> result = new ArrayList<String>();
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(urls.toString()).openStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(doc == null)
        {
            return null;
        }

        NodeList results = doc.getElementsByTagName("item");
        NodeList description = doc.getElementsByTagName("description");

        //add the main title to result
        result.add("<big>" + description.item(0).getTextContent() + ":</big><br><br>");

        for(int i = 0; i < results.getLength(); i++)
        {
            NodeList contents = results.item(i).getChildNodes();

            for(int j = 0; j < contents.getLength(); j++)
            {
                if(isChecked)
                {
                    if(contents.item(j).getNodeName().contains("title") && contents.item(j).getTextContent().toLowerCase().contains(" ".toLowerCase()))
                    {
                        //get the title of the warning, and add a linebreak inside it and capsulate the time and date
                        String title = contents.item(j).getTextContent();
                        StringBuilder sb = new StringBuilder(title);
                        sb.insert(title.indexOf(',') + 2, "<br>(");
                        sb.append(")");
                        //add the title to result
                        result.add("<br><b>" + sb.toString() + "</b>");

                        for(int k = 0; k < contents.getLength(); k++)
                        {
                            if(contents.item(k).getNodeName().contains("description"))
                            {
                                //add the additional info of the warning to result
                                result.add("<br>   -Lisätietoa: <dfn>" + contents.item(k).getTextContent() + "</dfn><br>");
                            }
                        }
                    }
                }
                else
                {
                    if(contents.item(j).getNodeName().contains("title") && (contents.item(j).getTextContent().toLowerCase().contains("koko maa".toLowerCase()) || contents.item(j).getTextContent().toLowerCase().contains("Vaasa".toLowerCase()) || contents.item(j).getTextContent().toLowerCase().contains("Pohjanmaa".toLowerCase())))
                    {
                        //get the title of the warning, and add a linebreak inside it and capsulate the time and date
                        String title = contents.item(j).getTextContent();
                        StringBuilder sb = new StringBuilder(title);
                        sb.insert(title.indexOf(',') + 2, "<br>(");
                        sb.append(")");
                        //add the title to result
                        result.add("<br><b>" + sb.toString() + "</b>");

                        for(int k = 0; k < contents.getLength(); k++)
                        {
                            if(contents.item(k).getNodeName().contains("description"))
                            {
                                //add the additional info of the warning to result
                                result.add("<br>   -Lisätietoa: <dfn>" + contents.item(k).getTextContent() + "</dfn><br>");
                            }
                        }
                    }
                }
            }

            if(i > 0)
            {
                publishProgress(100 / (results.getLength() / i));
            }
            else
            {
                publishProgress(0);
            }
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        Fragment1.UpdateTextfield("Ladataan XML dataa...: " + progress[0] + "%");
    }

    @Override
    protected void onPostExecute(List<String> result)
    {
        if(result != null)
        {
            Fragment1.UpdateTextfield(result);
        }
        else
        {
            Fragment1.UpdateTextfield("Ei internetyhteyttä");
        }
    }
}