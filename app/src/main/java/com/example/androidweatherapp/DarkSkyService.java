package com.example.androidweatherapp;

/*
Julia Romero
JRL5576
Project5
 */
import android.util.Log;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import tk.plogitech.darksky.api.jackson.DarkSkyJacksonClient;
import tk.plogitech.darksky.forecast.APIKey;
import tk.plogitech.darksky.forecast.ForecastException;
import tk.plogitech.darksky.forecast.ForecastRequest;
import tk.plogitech.darksky.forecast.ForecastRequestBuilder;
import tk.plogitech.darksky.forecast.GeoCoordinates;
import tk.plogitech.darksky.forecast.model.Forecast;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

public class DarkSkyService {
    private static final String TAG ="DarkSkyService";
    private static final String API_KEY = "e17d659fd234248ad409b79f40cba236";
    private static DarkSkyJacksonClient client;

    static {
        client = new DarkSkyJacksonClient();
    }

    public Forecast getForecast(double longitude, double latitude) throws ForecastException {
        ForecastRequest request = new ForecastRequestBuilder()
                .key(new APIKey(API_KEY))
                .location(new GeoCoordinates(new Longitude(longitude), new Latitude(latitude)))
                .extendHourly()
                .build();

        return client.forecast(request);
    };

    public CompletableFuture<Forecast> getForecastAsync(double longitude, double latitude) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return getForecast(longitude, latitude);
                } catch (ForecastException e) {
                    e.printStackTrace();
                    Log.w(TAG, e);
                    return null;
                }
            });
    }
    //Time Machine API Call
    public Forecast getTimeMachineForecast(double longitude, double latitude, Instant instant) throws ForecastException {
        ForecastRequest request = new ForecastRequestBuilder()
                .key(new APIKey(API_KEY))
                .location(new GeoCoordinates(new Longitude(longitude), new Latitude(latitude)))
                .time(instant)
                .extendHourly()
                .build();

        return client.forecast(request);
    };

    public CompletableFuture<Forecast> getTimeMachineForecastAsync(double longitude, double latitude, Instant instant) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getTimeMachineForecast(longitude, latitude, instant);
            } catch (ForecastException e) {
                e.printStackTrace();
                Log.w(TAG, e);
                return null;
            }
        });
    }

}
