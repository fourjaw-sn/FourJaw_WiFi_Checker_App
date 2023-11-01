package com.example.fourjaw_wifi_checker_app;


import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Locale;

public class LineChartXAxisValueFormatter extends IndexAxisValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        int value_int = (int) value;

        int total_hours = value_int/3600;
        int total_mins = (value_int%3600)/60;
        int total_seconds1 = ((value_int%3600)%60);

        return ((String.format(Locale.ENGLISH, "%02d", total_hours)))+":"+
                              ((String.format(Locale.ENGLISH, "%02d", total_mins)))+":"+
                              ((String.format(Locale.ENGLISH, "%02d", total_seconds1)));
    }
}
