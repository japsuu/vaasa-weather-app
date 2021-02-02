package com.japsu.vaasaweather;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Alerts
{
    static List<String> GetAlerts()
    {
        List<String> result = new ArrayList<>();
        try
        {
            new DownloadXML().execute(new URL("https://alerts.fmi.fi/cap/feed/rss_fi-FI.rss"));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}

class DownloadXML extends AsyncTask<URL, Integer, List<String>>
{
    @Override
    protected List<String> doInBackground(URL... urls)
    {
        Log.d("TEST", "Aloitetaan XML datan latausprosessi.");
        List<String> result = new ArrayList<>();
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(urls[0].toString()).openStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        NodeList results = doc.getElementsByTagName("item");
        NodeList description = doc.getElementsByTagName("description");

        result.add(description.item(0).getTextContent() + ":");

        for(int i = 0; i < results.getLength(); i++)
        {
            NodeList contents = results.item(i).getChildNodes();

            for(int j = 0; j < contents.getLength(); j++)
            {
                if(contents.item(j).getNodeName().contains("title") && (contents.item(j).getTextContent().contains("koko maa") || contents.item(j).getTextContent().contains("Vaasa") || contents.item(j).getTextContent().contains("Pohjanmaa")))
                {
                    result.add("\n" + contents.item(j).getTextContent());
                    String info = contents.item(j + 2).getTextContent().substring(contents.item(j + 2).getTextContent().indexOf(' ') + 1);
                    result.add("Lisätietoa: " + info + "\n");

                    publishProgress(100 / (results.getLength() + contents.getLength() - i * j));
                }
            }
        }

        return result;
    }

    protected void onProgressUpdate(Integer... progress)
    {
        Log.d("XML", "Ladataan XML dataa...: " + progress[0] + "\n");
    }

    protected void onPostExecute(List<String> result)
    {
        if(result != null)
        {
            MainActivity.UpdateWarnings(result);
        }
    }
}