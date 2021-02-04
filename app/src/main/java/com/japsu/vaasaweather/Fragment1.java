package com.japsu.vaasaweather;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class Fragment1 extends Fragment
{
    static TextView warningsView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Warnings.GetWarnings();
        View view = inflater.inflate(R.layout.fragment1_layout, container, false);
        warningsView = (TextView) view.findViewById(R.id.warningContents);
        warningsView.setMovementMethod(new ScrollingMovementMethod());
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
}
