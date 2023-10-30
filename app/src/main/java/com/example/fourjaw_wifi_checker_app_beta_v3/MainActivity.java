package com.example.fourjaw_wifi_checker_app_beta_v3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Button Reset_test_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Run wifi scan and return connection info
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int wifi_strength = wifiInfo.getRssi();
        TextView Wifi_details_values = findViewById(R.id.Wifi_details_values);
        TextView Wifi_details_properties_text = findViewById(R.id.Wifi_details_properties_text);
        TextView No_connection_warning_box = findViewById(R.id.No_connection_warning_box);
        TextView No_connection_warning_text = findViewById(R.id.No_connection_warning_text);
        TextView No_connection_warning_text_top = findViewById(R.id.No_connection_warning_text_top);
        Button Wifi_settings_button = findViewById(R.id.wifi_settings_button);


        Wifi_details_values.setVisibility(View.VISIBLE);
        Wifi_details_properties_text.setVisibility(View.VISIBLE);
        Wifi_settings_button.setVisibility(View.INVISIBLE);
        No_connection_warning_box.setVisibility(View.INVISIBLE);
        No_connection_warning_text.setVisibility(View.INVISIBLE);
        No_connection_warning_text_top.setVisibility(View.INVISIBLE);

        if (Objects.equals(wifi_strength, -127)){
            Wifi_details_values.setVisibility(View.INVISIBLE);
            Wifi_details_properties_text.setVisibility(View.INVISIBLE);
            Wifi_settings_button.setVisibility(View.VISIBLE);
            No_connection_warning_box.setVisibility(View.VISIBLE);
            No_connection_warning_text.setVisibility(View.VISIBLE);
            No_connection_warning_text_top.setVisibility(View.VISIBLE);

            Wifi_settings_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    //Ensure app name change in manifest file queries section too
                    PackageManager pm = MainActivity.this.getPackageManager();
                    if(isPackageInstalled("com.example.wifi_settings", pm)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.wifi_settings");
                        startActivity(launchIntent);
                    } else if (isPackageInstalled("com.android.settings", pm)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
                        startActivity(launchIntent);
                    } else{
                        Toast.makeText(MainActivity.this, "WiFi Settings App not installed, please contact FourJaw support",
                                Toast.LENGTH_LONG).show();
                    }

                }});
        }else{
            //Set connected wifi properties with results of wifi scan
            String enabled = "False";
            if (wifiManager.isWifiEnabled()) {
                enabled = "True";
            }
            String SSID = wifiInfo.getSSID().replace("\"", "");
            String BSSID = wifiInfo.getBSSID();
            String Frequency = "No signal";
            if ((float) wifiInfo.getFrequency() >= 0 && (float) wifiInfo.getFrequency() <= 4000) {
                Frequency = "2.4 GHz";
                //final DecimalFormat dfZero = new DecimalFormat("0.0");
                //Frequency = dfZero.format((float) wifiInfo.getFrequency() / 1000) + " GHz";
            }else if ((float) wifiInfo.getFrequency() >= 4000){
                Frequency = "5 GHz";
            }else if ((float) wifiInfo.getFrequency() >= 6000){
                Frequency = "6 GHz";
            }

            //Set wifi details text to results of scan
            Wifi_details_values.setText(getString(R.string.Wifi_details_values, enabled, SSID, BSSID, Frequency));
        }


        //Stop existing loops
        mHandler.removeCallbacks(Wifi_scan_loop);

        //Start the repeating loop
        Wifi_scan_loop.run();

        //Call check tablet permissions to check if location settings have been enabled
        checkPermission();

        //Enable reset button to reset the data and restart the wifi checking loop
        Reset_test_button = findViewById(R.id.Reset_test_button);
        Reset_test_button.setOnClickListener(view -> {
            Reset_data.setData("true");
            //System.out.println(Reset_data.getData());
            mHandler.removeCallbacks(Wifi_scan_loop);
            Wifi_scan_loop.run();
        });
    }
    @Override
    public void onBackPressed() {
        //your code when back button pressed
        super.onBackPressed();
        //System.out.println("Back button pressed");
//        startActivity(new Intent(Intent.CATEGORY_HOME));
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    // Background Location Permission is granted so do your work here
                    askPermissionForBackgroundUsage();
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission();
        }
    }

    int LOCATION_PERMISSION_CODE = 0;
    int BACKGROUND_LOCATION_PERMISSION_CODE = 0;

    private void askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed!")
                    .setMessage("Location Permission Needed!")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE))
                    .setNegativeButton("CANCEL", (dialog, which) -> {
                        // Permission is denied by the user
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed!")
                    .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_CODE))
                    .setNegativeButton("CANCEL", (dialog, which) -> {
                        // User declined for Background Location Permission.
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        // Background Location Permission is granted so do your work here
                        askPermissionForBackgroundUsage();
                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage();
                    }
                }
            } else {
                // User denied location permission
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
            } else {
                // User declined for Background Location Permission.
            }
        }

    }

    private final Runnable Wifi_scan_loop = new Runnable() {

        //Initialize variables and arrays
        private final ArrayList<Entry> graph_data_entries = new ArrayList<>();
        final ArrayList<String> network_change_array = new ArrayList<>();
        final ArrayList<Integer> network_change_timestamp_array = new ArrayList<>();
        final ArrayList<String> access_point_change_array = new ArrayList<>();
        final ArrayList<Integer> access_point_change_timestamp_array = new ArrayList<>();
        final ArrayList<Integer> wifi_strength_array = new ArrayList<>();
        String reference_timestamp = null;
        String display_reference_timestamp_date = null;
        Comparable<String> reference_date = null;
        int reference_timestamp_total_seconds = 0;

        @Override
        public void run() {

            //Delay time between loop
            mHandler.postDelayed(this, 5000);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            int wifi_strength = wifiInfo.getRssi();
            TextView Wifi_details_values = findViewById(R.id.Wifi_details_values);
            TextView Wifi_details_properties_text = findViewById(R.id.Wifi_details_properties_text);
            TextView No_connection_warning_box = findViewById(R.id.No_connection_warning_box);
            TextView No_connection_warning_text = findViewById(R.id.No_connection_warning_text);
            TextView No_connection_warning_text_top = findViewById(R.id.No_connection_warning_text_top);
            Button Wifi_settings_button = findViewById(R.id.wifi_settings_button);


            Wifi_details_values.setVisibility(View.VISIBLE);
            Wifi_details_properties_text.setVisibility(View.VISIBLE);
            Wifi_settings_button.setVisibility(View.INVISIBLE);
            No_connection_warning_box.setVisibility(View.INVISIBLE);
            No_connection_warning_text.setVisibility(View.INVISIBLE);
            No_connection_warning_text_top.setVisibility(View.INVISIBLE);

            if (Objects.equals(wifi_strength, -127)) {
                Wifi_details_values.setVisibility(View.INVISIBLE);
                Wifi_details_properties_text.setVisibility(View.INVISIBLE);
                Wifi_settings_button.setVisibility(View.VISIBLE);
                No_connection_warning_box.setVisibility(View.VISIBLE);
                No_connection_warning_text.setVisibility(View.VISIBLE);
                No_connection_warning_text_top.setVisibility(View.VISIBLE);

                Wifi_settings_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        //Ensure app name change in manifest file queries section too
                        PackageManager pm = MainActivity.this.getPackageManager();
                        if (isPackageInstalled("com.example.wifi_settings", pm)) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.wifi_settings");
                            startActivity(launchIntent);
                        } else if (isPackageInstalled("com.android.settings", pm)) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
                            startActivity(launchIntent);
                        } else {
                            Toast.makeText(MainActivity.this, "WiFi Settings App not installed, please contact FourJaw support",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
            } else {
                //Set connected wifi properties with results of wifi scan
                String enabled = "False";
                if (wifiManager.isWifiEnabled()) {
                    enabled = "True";
                }
                String SSID = wifiInfo.getSSID().replace("\"", "");
                String BSSID = wifiInfo.getBSSID();
                String Frequency = "No signal";
                if ((float) wifiInfo.getFrequency() >= 0 && (float) wifiInfo.getFrequency() <= 4000) {
                    Frequency = "2.4 GHz";
                    //final DecimalFormat dfZero = new DecimalFormat("0.0");
                    //Frequency = dfZero.format((float) wifiInfo.getFrequency() / 1000) + " GHz";
                } else if ((float) wifiInfo.getFrequency() >= 4000) {
                    Frequency = "5 GHz";
                } else if ((float) wifiInfo.getFrequency() >= 6000) {
                    Frequency = "6 GHz";
                }

                //Set wifi details text to results of scan
                Wifi_details_values.setText(getString(R.string.Wifi_details_values, enabled, SSID, BSSID, Frequency));
            }

//            //Run wifi scan and return connection info
//            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//
//            //Set connected wifi properties with results of wifi scan
//            int wifi_strength = wifiInfo.getRssi();
//            String enabled = "False";
//            if (wifiManager.isWifiEnabled()) {
//                enabled = "True";
//            }
            String SSID = wifiInfo.getSSID().replace("\"", "");
            String BSSID = wifiInfo.getBSSID();
//
//            String Frequency = "No signal";
//            if ((float) wifiInfo.getFrequency() >= 0 && (float) wifiInfo.getFrequency() <= 4000) {
//                Frequency = "2.4 GHz";
//                //final DecimalFormat dfZero = new DecimalFormat("0.0");
//                //Frequency = dfZero.format((float) wifiInfo.getFrequency() / 1000) + " GHz";
//            }else if ((float) wifiInfo.getFrequency() >= 4000){
//                Frequency = "5 GHz";
//            }else if ((float) wifiInfo.getFrequency() >= 6000){
//                Frequency = "6 GHz";
//            }
//
//            //Set wifi details text to results of scan
//            TextView Wifi_details_values = findViewById(R.id.Wifi_details_values);
//            Wifi_details_values.setText(getString(R.string.Wifi_details_values, enabled, SSID, BSSID, Frequency));

            //Run available networks scan and return info
            wifiManager.startScan();

            //List<ScanResult> availNetworks = wifiManager.getScanResults();


            List<ScanResult> availNetworks = wifiManager.getScanResults();
            //System.out.println("list" + availNetworks);
            Collections.sort(availNetworks, new Signal_Strength_Comparator());
            //System.out.println("sorted list" + availNetworks);

            // Other networks section

            //Enabled location checks
            TextView Location_services_warning_box = findViewById(R.id.Location_services_warning_box);
            TextView Location_services_warning_text = findViewById(R.id.Location_services_warning_text);
            if (Objects.equals(BSSID, "02:00:00:00:00:00")) {
                Location_services_warning_box.setVisibility(View.VISIBLE);
                Location_services_warning_text.setVisibility(View.VISIBLE);
            } else {
                Location_services_warning_box.setVisibility(View.INVISIBLE);
                Location_services_warning_text.setVisibility(View.INVISIBLE);
            }


            // Table section

            //Initialise other networks table
            TableLayout Other_Networks_table = findViewById(R.id.Other_Networks_table);
            //remove all rows except the placeholder row
            Other_Networks_table.removeViews(1, Math.max(0, Other_Networks_table.getChildCount() - 1));
            //Initialise row array's using the number of available networks
            TextView[] textArray = new TextView[availNetworks.size()];
            TableRow[] tr_head = new TableRow[availNetworks.size()];
            ImageView[] ImageView = new ImageView[availNetworks.size()];


            for (int i = 0; i < availNetworks.size(); i++) {

                //Set network properties with results of networks scan
                String SSID_text = availNetworks.get(i).SSID;
                //Number of characters to be shown
                int number_of_characters = 18;
                if (SSID_text.length() > number_of_characters) {
                    SSID_text = SSID_text.substring(0, number_of_characters - 3) + "...";
                }
                String BSSID_text = availNetworks.get(i).BSSID;
                int Strength_text = availNetworks.get(i).level;

                //String Frequency_text = (dfZero.format((float) availNetworks.get(i).frequency / 1000) + " GHz");
                String Frequency_text = null;
                if ((float) availNetworks.get(i).frequency >= 0 && (float) availNetworks.get(i).frequency <= 4000) {
                    Frequency_text = "2.4 GHz";
                } else if ((float) availNetworks.get(i).frequency >= 4000) {
                    Frequency_text = "5 GHz";
                } else if ((float) availNetworks.get(i).frequency >= 6000) {
                    Frequency_text = "6 GHz";
                }


                //Create the table rows
                tr_head[i] = new TableRow(MainActivity.this);
                tr_head[i].setId(i + 1);

                tr_head[i].setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                // Table text settings
                Typeface table_typeface = ResourcesCompat.getFont(MainActivity.this, R.font.roboto);
                if (Objects.equals(BSSID_text, BSSID)) {
                    table_typeface = ResourcesCompat.getFont(MainActivity.this, R.font.roboto_bold);
                    wifi_strength = Strength_text;
                }


                int text_color = ContextCompat.getColor(MainActivity.this, R.color.text_black);

                //SSID properties
                textArray[i] = new TextView(MainActivity.this);
                textArray[i].setId(i + 111);
                textArray[i].setText(SSID_text);
                textArray[i].setTextColor(text_color);
                textArray[i].setTypeface(table_typeface);
                textArray[i].setTextSize(15);
                tr_head[i].addView(textArray[i]);

                //Access point MAC address properties
                textArray[i] = new TextView(MainActivity.this);
                textArray[i].setId(i + 112);
                textArray[i].setText(BSSID_text);
                textArray[i].setTextColor(text_color);
                textArray[i].setTypeface(table_typeface);
                textArray[i].setTextSize(15);
                tr_head[i].addView(textArray[i]);

                //Strength icon properties
                ImageView[i] = new ImageView(MainActivity.this);    // part3
                ImageView[i].setId(i + 113);// define id that must be unique
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(25, 25);
                ImageView[i].setLayoutParams(layoutParams);
                ImageView[i].setTranslationY(5);
                if (Strength_text > -60) {
                    ImageView[i].setImageResource(R.drawable.baseline_done_24);
                    ImageView[i].setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_green, null));
                    ImageView[i].setForegroundGravity(Gravity.CENTER_VERTICAL);

                } else if (Strength_text > -70) {
                    ImageView[i].setImageResource(R.drawable.baseline_horizontal_rule_24);
                    ImageView[i].setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_orange, null));
                    ImageView[i].setForegroundGravity(Gravity.CENTER_VERTICAL);

                } else {
                    ImageView[i].setImageResource(R.drawable.baseline_clear_24);
                    ImageView[i].setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_red, null));
                    ImageView[i].setForegroundGravity(Gravity.CENTER_VERTICAL);
                }
                tr_head[i].addView(ImageView[i]); // add the column to the table row here

                //Strength text properties
                textArray[i] = new TextView(MainActivity.this);    // part3
                textArray[i].setId(i + 114);// define id that must be unique
                textArray[i].setText(getString(R.string.Other_networks_table_Strength_text, Integer.toString(Strength_text))); // set the text for the header
                textArray[i].setTextColor(text_color);
                textArray[i].setTypeface(table_typeface);
                textArray[i].setTextSize(15);
                //textArray[i].setPadding(5, 5, 5, 5); // set the padding (if required)
                tr_head[i].addView(textArray[i]); // add the column to the table row here

                //Frequency text properties
                textArray[i] = new TextView(MainActivity.this);    // part3
                textArray[i].setId(i + 115);// define id that must be unique
                textArray[i].setText(Frequency_text); // set the text for the header
                textArray[i].setTextColor(text_color);
                textArray[i].setTypeface(table_typeface);
                textArray[i].setTextSize(15);
                //textArray[i].setPadding(5, 5, 5, 5); // set the padding (if required)
                tr_head[i].addView(textArray[i]); // add the column to the table row here

                Other_Networks_table.addView(tr_head[i], new TableLayout.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
            }


            //Time section

            SimpleDateFormat hours = new SimpleDateFormat("HH", Locale.ENGLISH);
            String hoursInString = hours.format(new Date());
            int int_hours = Integer.parseInt(hoursInString);
            //System.out.println("hours: " + int_hours);
//
            SimpleDateFormat minutes = new SimpleDateFormat("mm", Locale.ENGLISH);
            String minutesInString = minutes.format(new Date());
            int int_minutes = Integer.parseInt(minutesInString);
            //System.out.println("minutes: " + int_minutes);
//
            SimpleDateFormat seconds = new SimpleDateFormat("ss", Locale.ENGLISH);
            String secondsInString = seconds.format(new Date());
            int int_seconds = Integer.parseInt(secondsInString);
            //System.out.println("seconds: " + int_seconds);

            int total_seconds = int_hours * 3600 + int_minutes * 60 + int_seconds;
            //System.out.println("total_seconds: " + total_seconds);

            SimpleDateFormat date_day_format = new SimpleDateFormat("dd", Locale.ENGLISH);
            Comparable<String> current_date = date_day_format.format(new Date());

            //System.out.println("reference_timestamp: " + reference_timestamp);


            if (reference_timestamp == null) {
                SimpleDateFormat time_format = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.ENGLISH);
                reference_timestamp = time_format.format(new Date());

                reference_date = date_day_format.format(new Date());
                System.out.println("reference_date: " + reference_date);

                SimpleDateFormat reference_timestamp_hours = new SimpleDateFormat("HH", Locale.ENGLISH);
                String reference_timestamp_hoursInString = reference_timestamp_hours.format(new Date());
                int reference_timestamp_int_hours = Integer.parseInt(reference_timestamp_hoursInString);

                SimpleDateFormat reference_timestamp_minutes = new SimpleDateFormat("mm", Locale.ENGLISH);
                String reference_timestamp_minutesInString = reference_timestamp_minutes.format(new Date());
                int reference_timestamp_int_minutes = Integer.parseInt(reference_timestamp_minutesInString);

                SimpleDateFormat reference_timestamp_seconds = new SimpleDateFormat("ss", Locale.ENGLISH);
                String reference_timestamp_secondsInString = reference_timestamp_seconds.format(new Date());
                int reference_timestamp_int_seconds = Integer.parseInt(reference_timestamp_secondsInString);

                reference_timestamp_total_seconds = reference_timestamp_int_hours * 3600 + reference_timestamp_int_minutes * 60 + reference_timestamp_int_seconds;
                //System.out.println("reference_timestamp total_seconds: " + reference_timestamp_total_seconds);
            }

            //Reset button section

            if (Objects.equals(Reset_data.getData(), "true")) {
                graph_data_entries.clear();
                wifi_strength_array.clear();
                Reset_data.setData("false");
                reference_timestamp = null;
                network_change_array.clear();
                network_change_timestamp_array.clear();
                access_point_change_array.clear();
                access_point_change_timestamp_array.clear();
            }

//            //Total amount of seconds in a day is 86400
//            if (total_seconds >= 86370 || total_seconds <= 30) {
//                graph_data_entries.clear();
//                wifi_strength_array.clear();
//                reference_timestamp = null;
//                network_change_array.clear();
//                network_change_timestamp_array.clear();
//                access_point_change_array.clear();
//                access_point_change_timestamp_array.clear();
//            }

            System.out.println("current_date "+ current_date);
            System.out.println("reference_date "+ reference_date);

            if (!current_date.equals(reference_date)) {
                graph_data_entries.clear();
                wifi_strength_array.clear();
                reference_timestamp = null;
                network_change_array.clear();
                network_change_timestamp_array.clear();
                access_point_change_array.clear();
                access_point_change_timestamp_array.clear();
                //System.out.println("time reset triggered");
            }

//            int time_test = total_seconds - reference_timestamp_total_seconds;
//            System.out.println("time_test: " + time_test);
//            if (time_test < 0) {
//                graph_data_entries.clear();
//                wifi_strength_array.clear();
//                reference_timestamp = null;
//                network_change_array.clear();
//                network_change_timestamp_array.clear();
//                access_point_change_array.clear();
//                access_point_change_timestamp_array.clear();
//                //System.out.println("time reset triggered");
//            }

            //Chart section

            CombinedChart mChart = findViewById(R.id.Timeline);

            //Initialising line chart data
            if (reference_timestamp != null) {
                graph_data_entries.add(new Entry(total_seconds, wifi_strength));
            }
            LineDataSet wifi_line_data_set = new LineDataSet(graph_data_entries, "Line DataSet");

            //System.out.println("graph_data_entries: " + graph_data_entries);

            //line properties
            wifi_line_data_set.setDrawFilled(false);
            wifi_line_data_set.setFillColor(ContextCompat.getColor(MainActivity.this, R.color.FourJaw_purple));
            wifi_line_data_set.setColor(ContextCompat.getColor(MainActivity.this, R.color.FourJaw_purple));
            wifi_line_data_set.setCircleColor(ContextCompat.getColor(MainActivity.this, R.color.FourJaw_purple));
            wifi_line_data_set.setDrawCircleHole(false);
            wifi_line_data_set.setLineWidth(2f);
            wifi_line_data_set.setCircleRadius(2f);
            wifi_line_data_set.setValueTextSize(0f);
            wifi_line_data_set.enableDashedLine(20F, 0F, 0F);


            LineData wifi_line_data = new LineData();
            wifi_line_data.addDataSet(wifi_line_data_set);

            //Bar chart section (can be removed, kept for reference)
//            entries1.add(new BarEntry(total_seconds, new float[]{-67, -10, -60}));
//            int[] colorClassArray = new int[]{
//                    ContextCompat.getColor(MainActivity.this, R.color.graph_red),
//                    ContextCompat.getColor(MainActivity.this, R.color.graph_orange),
//                    ContextCompat.getColor(MainActivity.this, R.color.graph_green)};
//
//            BarDataSet barDataSet = new BarDataSet(entries1, "Bar Set");
//            barDataSet.setColors(colorClassArray);
//            barDataSet.setValueTextSize(0f);
//            float barWidth = 5f; // x2 dataset
//
//            BarData bar_data = new BarData(barDataSet);
//            bar_data.setBarWidth(barWidth);


            CombinedData data = new CombinedData();
            data.setData(wifi_line_data);
            //data.setData(bar_data);

            mChart.setData(data);

            mChart.notifyDataSetChanged();
            mChart.invalidate();

            //Chart properties
            mChart.getDescription().setText("");
            mChart.getDescription().setTextColor(Color.RED);
            mChart.getLegend().setEnabled(false);
            mChart.setTouchEnabled(false);
            Typeface tfRegular = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tfRegular = getResources().getFont(R.font.montserrat);
            }


            XAxis xAxis;
            {   // // X-Axis Style // //
                xAxis = mChart.getXAxis();

                // X axis properties
                xAxis.enableGridDashedLine(10f, 10f, 0f);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                // Formatter to adjust epoch time to readable date
                xAxis.setValueFormatter(new LineChartXAxisValueFormatter());
                xAxis.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_black));
                xAxis.setTypeface(tfRegular);
                xAxis.setLabelCount(7);
                xAxis.setGranularity(1f);

                //Network change process, stores network changes in an array that are then used to draw limit lines on the graph
                if (reference_timestamp != null) {
                    if (network_change_array.size() == 0) {
                        network_change_array.add(SSID);
                        network_change_timestamp_array.add(total_seconds);
                    }
                    if (!SSID.equals(network_change_array.get(network_change_array.size() - 1))) {
                        network_change_array.add(SSID);
                        network_change_timestamp_array.add(total_seconds);
                    }

                    if (BSSID != null) {
                        if (access_point_change_array.size() == 0) {
                            access_point_change_array.add(BSSID);
                            access_point_change_timestamp_array.add(total_seconds);
                        }
                        if (!BSSID.equals(access_point_change_array.get(access_point_change_array.size() - 1))) {
                            access_point_change_array.add(BSSID);
                            access_point_change_timestamp_array.add(total_seconds);
                        }
                    } else {
                        if (access_point_change_array.size() == 0) {
                            access_point_change_array.add("Wifi disabled");
                            access_point_change_timestamp_array.add(total_seconds);
                        }
                        if (!Objects.equals(access_point_change_array.get(access_point_change_array.size() - 1), "Wifi disabled")) {
                            access_point_change_array.add("Wifi disabled");
                            access_point_change_timestamp_array.add(total_seconds);
                        }
                    }


                    //graph limit lines

                    // reset all limit lines to avoid overlapping lines
                    xAxis.removeAllLimitLines();

                    // add limit lines for each network change
                    for (int network_change_array_i = 0; network_change_array_i < network_change_array.size(); network_change_array_i++) {

                        LimitLine network_change_limitline = new LimitLine(network_change_timestamp_array.get(network_change_array_i), "");
                        network_change_limitline.setLineColor(Color.RED);
                        network_change_limitline.setLineWidth(2f);

                        xAxis.addLimitLine(network_change_limitline);
                        //xAxis.setDrawLimitLinesBehindData(true);
                    }

                    // add limit lines for each access point change with check to ensure that if network and access point change occurs don't draw both lines
                    for (int access_point_change_array_i = 0; access_point_change_array_i < access_point_change_array.size(); access_point_change_array_i++) {

                        if (network_change_timestamp_array.stream().noneMatch(access_point_change_timestamp_array.get(access_point_change_array_i)::equals)) {
                            LimitLine ll = new LimitLine(access_point_change_timestamp_array.get(access_point_change_array_i), "");
                            ll.setLineColor(Color.RED);
                            ll.enableDashedLine(10f, 10f, 0f);
                            ll.setLineWidth(2f);
                            xAxis.addLimitLine(ll);
                            //xAxis.setDrawLimitLinesBehindData(true);
                        }
                    }
                }
            }

            YAxis yAxis;
            {   // // Y-Axis Style // //
                yAxis = mChart.getAxisLeft();
                yAxis.setTypeface(tfRegular);
                yAxis.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.text_black));

                // disable dual axis (only use LEFT axis)
                mChart.getAxisRight().setEnabled(false);

                // horizontal grid lines
                yAxis.enableGridDashedLine(10f, 10f, 0f);

                // axis range
                yAxis.setAxisMaximum(-30f);
                yAxis.setAxisMinimum(-127f);

            }


            TextView Timeline_current_strength_text = findViewById(R.id.Timeline_current_strength_text);

            if (Objects.equals(wifi_strength, -127)) {
                Timeline_current_strength_text.setText(getString(R.string.Timeline_current_strength_no_connection_text));
            } else {
                Timeline_current_strength_text.setText(getString(R.string.Timeline_current_strength_text, wifi_strength));
            }

            TextView Connection_Quality_good_percentage = findViewById(R.id.Connection_Quality_good_percentage);
            TextView connection_Quality_average_percentage = findViewById(R.id.connection_Quality_average_percentage);
            TextView connection_Quality_bad_percentage = findViewById(R.id.connection_Quality_bad_percentage);

            if (reference_timestamp != null) {
                wifi_strength_array.add(wifi_strength);
            }

            float good_threshold = -60;
            float good_count = 0;
            float average_threshold = -70;
            float average_count = 0;
            float bad_count = 0;

            for (int i = 0; i < wifi_strength_array.size(); i++) {
                if (wifi_strength_array.get(i) > good_threshold) {
                    good_count++;
                } else if (wifi_strength_array.get(i) > average_threshold) {
                    average_count++;
                } else {
                    bad_count++;
                }
            }

            float good_percentage = ((good_count / wifi_strength_array.size()) * 100);
            int good_percentage_int = (int) good_percentage;
            //Connection_Quality_good_percentage.setText(good_percentage_int + "%");
            Connection_Quality_good_percentage.setText(getString(R.string.Connection_quality_good_percentage_text, good_percentage_int));

            float average_percentage = ((average_count / wifi_strength_array.size()) * 100);
            int average_percentage_int = (int) average_percentage;
            connection_Quality_average_percentage.setText(getString(R.string.connection_Quality_average_percentage_text, average_percentage_int));

            float bad_percentage = ((bad_count / wifi_strength_array.size()) * 100);
            int bad_percentage_int = (int) bad_percentage;
            connection_Quality_bad_percentage.setText(getString(R.string.connection_Quality_bad_percentage_text, bad_percentage_int));


            TextView Connection_Quality_outcome_text = findViewById(R.id.Connection_Quality_outcome_text);
            ImageView Connection_Quality_Icon = findViewById(R.id.Connection_Quality_Icon);
            ImageView connection_Quality_Icon_box = findViewById(R.id.connection_Quality_Icon_box);
            TextView Minimum_results_box = findViewById(R.id.Minimum_results_box);
            TextView Minimum_results_box_text = findViewById(R.id.Minimum_results_text);
            TextView minimum_results_time_text = findViewById(R.id.minimum_results_time_text);
            Reset_test_button = findViewById(R.id.Reset_test_button);

            if (wifi_strength_array.size() < 6) {
                Connection_Quality_outcome_text.setText(getString(R.string.Connection_Quality_outcome_waiting_text));
                connection_Quality_Icon_box.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round, null));
                Connection_Quality_Icon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_transparent, null));
                Connection_Quality_Icon.setImageResource(R.drawable.baseline_update_24);
                Minimum_results_box.setVisibility(View.VISIBLE);
                Minimum_results_box_text.setVisibility(View.VISIBLE);
                minimum_results_time_text.setVisibility(View.VISIBLE);
                Reset_test_button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.purple_box_disabled, null));
                int time_remaining = 30 - (wifi_strength_array.size()) * 5;
                minimum_results_time_text.setText(getString(R.string.minimum_results_time_text, time_remaining));

            } else {
                Minimum_results_box.setVisibility(View.INVISIBLE);
                Minimum_results_box_text.setVisibility(View.INVISIBLE);
                minimum_results_time_text.setVisibility(View.INVISIBLE);
                Reset_test_button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.purple_box, null));

                if (bad_percentage_int > 10) {
                    Connection_Quality_outcome_text.setText(getString(R.string.Connection_Quality_outcome_bad_text));
                    connection_Quality_Icon_box.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_red, null));
                    Connection_Quality_Icon.setImageResource(R.drawable.baseline_clear_24);

                } else if (average_percentage_int > 20) {
                    Connection_Quality_outcome_text.setText(R.string.Connection_Quality_outcome_average_text);
                    connection_Quality_Icon_box.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_orange, null));
                    Connection_Quality_Icon.setImageResource(R.drawable.baseline_horizontal_rule_24);

                } else {
                    Connection_Quality_outcome_text.setText(R.string.Connection_Quality_outcome_good_text);
                    connection_Quality_Icon_box.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_green, null));
                    Connection_Quality_Icon.setImageResource(R.drawable.baseline_done_24);
                }
            }

            TextView Connection_quality_time_text = findViewById(R.id.Connection_quality_time_text);
            Connection_quality_time_text.setText(getString(R.string.Connection_quality_time_text, reference_timestamp));
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);

        //Run wifi scan and return connection info
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        //Set connected wifi properties with results of wifi scan
        String enabled = "False";
        if (wifiManager.isWifiEnabled()){
            enabled = "True";
        }
        String SSID = wifiInfo.getSSID().replace("\"", "");
        String BSSID = wifiInfo.getBSSID();
        String Frequency = "No signal";
        if ((float) wifiInfo.getFrequency() >= 0 && (float) wifiInfo.getFrequency() <= 4000) {
            Frequency = "2.4 GHz";
            //final DecimalFormat dfZero = new DecimalFormat("0.0");
            //Frequency = dfZero.format((float) wifiInfo.getFrequency() / 1000) + " GHz";
        }else if ((float) wifiInfo.getFrequency() >= 4000){
            Frequency = "5 GHz";
        }else if ((float) wifiInfo.getFrequency() >= 6000){
            Frequency = "6 GHz";
        }

        //Set wifi details text to results of scan
        TextView Wifi_details_values = findViewById(R.id.Wifi_details_values);
        Wifi_details_values.setText(getString(R.string.Wifi_details_values, enabled, SSID, BSSID, Frequency));

        Reset_test_button = findViewById(R.id.Reset_test_button);
        Reset_test_button.setOnClickListener(view -> {
            Reset_data.setData("true");
            //System.out.println(Reset_data.getData());
            mHandler.removeCallbacks(Wifi_scan_loop);
            Wifi_scan_loop.run();
        });
    }

    class Signal_Strength_Comparator implements Comparator<ScanResult> {
        @Override
        public int compare(ScanResult scan0, ScanResult scan1) {
            return scan1.level-scan0.level;
        }
    }
}