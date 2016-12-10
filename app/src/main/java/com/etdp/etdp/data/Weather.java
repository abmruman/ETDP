package com.etdp.etdp.data;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.etdp.etdp.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

public class Weather extends JsonConverter {
	private static final String W_API_KEY = BuildConfig.W_API_KEY;
	private static final String URL = "http://api.openweathermap.org/data/2.5/weather";

	@SerializedName("weather")
	private List<Row> rows;
	@SerializedName("cod")
	private int status;
	@SerializedName("dt")
	private long timestamp;

	public static Weather fromJson(String s) {
		return new Gson().fromJson(s, Weather.class);
	}

	public static Weather fetch(Location location) {
		String lat = String.format(
				Locale.ENGLISH,
				"%f",
				location.getLatitude()
		);
		String lon = String.format(
				Locale.ENGLISH,
				"%f",
				location.getLongitude()
		);
		Log.d("Weather:", "fetch: (" + lat + ", " + lon + ")");
		return fetch(lat, lon);
	}

	public static Weather fetch(String lat, String lon) {
		String uri = String.format(
				"%s?units=%s&lat=%s&lon=%s&appid=%s",
				URL,
				DatabaseContract.UNIT,
				Uri.encode(lat),
				Uri.encode(lon),
				W_API_KEY
		);
		try {
			URL url = new URL(uri);
			URLConnection urlConnection = url.openConnection();
			InputStream inputStream = urlConnection.getInputStream();

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder stringBuilder = new StringBuilder();
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			return fromJson(stringBuilder.toString());
		} catch (Exception e) {
			Log.e("Weather: ", "fetch: " + e.toString());
		}
		return null;
	}

	public int getStatus() {
		return status;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public List<Row> getRows() {
		return rows;
	}

	public class Row extends JsonConverter {
		@SerializedName("id")
		private int id;
		@SerializedName("main")
		private String condition;

		public int getId() {
			return id;
		}

		public String getCondition() {
			return condition;
		}
	}

}
