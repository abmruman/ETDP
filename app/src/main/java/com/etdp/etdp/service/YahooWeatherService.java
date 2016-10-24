package com.etdp.etdp.service;

import android.net.Uri;
import android.os.AsyncTask;

import com.etdp.etdp.data.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by test on 10/23/2016.
 */

public class YahooWeatherService  {
    private WeatherServiceCallback callback;
    private String location;
    private Exception error;

    public YahooWeatherService(WeatherServiceCallback callback) {
        this.callback = callback;
    }

    public String getLocation () {
        return location;
    }

    public void refreshWeather(String loc) {
        this.location = loc;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String jql = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", strings[0]);

                String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(jql));
                try {
                    URL url = new URL(endpoint);

                    URLConnection urlConnection = url.openConnection();

                    InputStream inputStream = urlConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    return stringBuilder.toString();
                } catch (Exception e) {
                    error = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                if(s == null && error != null) {
                    callback.serviceFailure(error);
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    /* Check whether city exist */
                    JSONObject queryResult = jsonObject.optJSONObject("query");

                    int count = queryResult.optInt("count");
                    if(count == 0) {
                        callback.serviceFailure(new LocationWeatherException("No weather information found for "+location));
                        return;
                    }

                    Channel channel = new Channel();
                    channel.populate(queryResult.optJSONObject("results").optJSONObject("channel"));

                    callback.serviceSuccess(channel);
                } catch (JSONException e) {
                    callback.serviceFailure(e);
                }
            }
        }.execute(location);
    }

    public class LocationWeatherException extends Exception {
        public LocationWeatherException(String detailMessage) {
            super(detailMessage);
        }
    }
}
