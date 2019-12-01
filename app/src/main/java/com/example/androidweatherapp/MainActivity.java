package com.example.androidweatherapp;
/*
Julia Romero
JRL5576
Project5
 */
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import tk.plogitech.darksky.forecast.model.Forecast;
import tk.plogitech.darksky.forecast.model.HourlyDataPoint;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private static final long FASTEST_INTERVAL = 2000; /* 2 sec */

    private DarkSkyService darkSkyService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        darkSkyService = new DarkSkyService();

        Log.i("Start", "Success in starting");

        MainActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
        MainActivityPermissionsDispatcher.getLastLocationWithPermissionCheck(this);

    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(mLocationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                // do work here
                                onLocationChanged(locationResult.getLastLocation());
                            }
                        },
                        Looper.myLooper());
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void getLastLocation() {
        // Get coordinates of last known location
        LocationServices.getFusedLocationProviderClient(getApplicationContext())
                .getLastLocation()
                .addOnSuccessListener(this, (location) -> {
                    // Call dark sky api
                    Log.i("Location", "Success in getting location");
                    if (location != null) {
                        Forecast forecast = darkSkyService.getForecastAsync(
                                location.getLongitude(),
                                location.getLatitude()).join();
                        handleForecast(forecast);
                    }
                })
                .addOnFailureListener((exception) -> {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    exception.printStackTrace();
                });
    }

    public void handleForecast(Forecast forecast) {
        if (forecast == null) {
            // show error view
            Log.w("MAP", "OOPS");
        }

        // show weather data
        displayForecast(forecast);

        Button button_send = (Button) this.findViewById(R.id.button_send);
        button_send.setOnClickListener((view) -> {
            EditText txt1 = findViewById(R.id.inputDate);
            EditText txt2 = findViewById(R.id.inputHour);
            String datetime = txt1.getText().toString() + "T" + txt2.getText().toString() + ":00:00Z";
            // [YYYY]-[MM]-[DD]T[HH]:[MM]:[SS]
            Instant instant;
            try {
                Log.i("Instant Parse", datetime);
                instant = Instant.parse(datetime);
            } catch (DateTimeParseException e) {
                TextView error = (TextView) findViewById(R.id.errortext);
                error.setVisibility(View.VISIBLE);
                return;
            }

            LocationServices.getFusedLocationProviderClient(getApplicationContext())
                    .getLastLocation()
                    .addOnSuccessListener(this, (location) -> {
                        // Call dark sky api
                        Log.i("Location", "Success in getting location");
                        if (location != null) {
                            Forecast timeMachineforecast = darkSkyService.getTimeMachineForecastAsync(
                                    location.getLongitude(),
                                    location.getLatitude(),
                                    instant).join();

                            TextView error = (TextView) findViewById(R.id.errortext);
                            error.setVisibility(View.INVISIBLE);
                            Resources res = getResources();
                            TextView timeMachineview = (TextView) findViewById(R.id.timeMachineTemp);
                            String temp = res.getString(R.string.timeMachinetemp, timeMachineforecast.getCurrently().getTemperature() * 9 / 5 + 32);
                            timeMachineview.setText(temp);
                            timeMachineview.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener((exception) -> {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        exception.printStackTrace();
                    });
        });
    }


    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void displayForecast(Forecast forecast) {
        setContentView(R.layout.summary_weather);

        Resources res = getResources();
        String temp = res.getString(R.string.temp,
                forecast.getCurrently().getTemperature() * 9 / 5 + 32.0,
                forecast.getCurrently().getTemperature());
        double avg = forecast.getHourly().getData().stream()
                .mapToDouble(HourlyDataPoint::getTemperature).sum() / forecast.getHourly().getData().size();
        TextView tempView = (TextView) findViewById(R.id.temp);
        Log.i("TempView", tempView.toString());
        tempView.setText(temp);


        String avgTemp = res.getString(R.string.avgTemp, avg * 9 / 5 + 32, avg);
        ((TextView) findViewById(R.id.avgTemp)).setText(avgTemp);

        String hum = res.getString(R.string.hum, forecast.getCurrently().getHumidity());
        ((TextView) findViewById(R.id.hum)).setText(hum);

        String rain = res.getString(R.string.rain, forecast.getCurrently().getPrecipProbability() * 100);
        ((TextView) findViewById(R.id.rain)).setText(rain);

        String wind = res.getString(R.string.wind, forecast.getCurrently().getWindSpeed());
        ((TextView) findViewById(R.id.wind)).setText(wind);

        // set temp for next 5 hours
        String temp1 = res.getString(R.string.temp1, forecast.getHourly().getData().get(0).getTemperature() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.hour1)).setText(temp1);

        String temp2 = res.getString(R.string.temp2, forecast.getHourly().getData().get(1).getTemperature() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.hour2)).setText(temp2);

        String temp3 = res.getString(R.string.temp3, forecast.getHourly().getData().get(2).getTemperature() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.hour3)).setText(temp3);

        String temp4 = res.getString(R.string.temp4, forecast.getHourly().getData().get(3).getTemperature() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.hour4)).setText(temp4);

        String temp5 = res.getString(R.string.temp5, forecast.getHourly().getData().get(4).getTemperature() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.hour5)).setText(temp5);

        // set temp for next 6 days
        // Set foredast temperature highs
        String day1 = res.getString(R.string.day1temp, forecast.getDaily().getData().get(1).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day1)).setText(day1);

        String day2 = res.getString(R.string.day2temp, forecast.getDaily().getData().get(2).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day2)).setText(day2);

        String day3 = res.getString(R.string.day3temp, forecast.getDaily().getData().get(3).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day3)).setText(day3);

        String day4 = res.getString(R.string.day4temp, forecast.getDaily().getData().get(4).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day4)).setText(day4);

        String day5 = res.getString(R.string.day5temp, forecast.getDaily().getData().get(5).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day5)).setText(day5);

        String day6 = res.getString(R.string.day6temp, forecast.getDaily().getData().get(6).getTemperatureHigh() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day6)).setText(day6);

        // Set forecast temperature lows
        String day1low = res.getString(R.string.day1low, forecast.getDaily().getData().get(1).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day1low)).setText(day1low);

        String day2low = res.getString(R.string.day2low, forecast.getDaily().getData().get(2).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day2low)).setText(day2low);

        String day3low = res.getString(R.string.day3low, forecast.getDaily().getData().get(3).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day3low)).setText(day3low);

        String day4low = res.getString(R.string.day4low, forecast.getDaily().getData().get(4).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day4low)).setText(day4low);

        String day5low = res.getString(R.string.day5low, forecast.getDaily().getData().get(5).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day5low)).setText(day5low);

        String day6low = res.getString(R.string.day6low, forecast.getDaily().getData().get(6).getTemperatureLow() * 9 / 5 + 32.0);
        ((TextView) findViewById(R.id.day6low)).setText(day6low);

    }
}
