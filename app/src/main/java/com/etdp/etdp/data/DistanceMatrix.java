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

public class DistanceMatrix extends JsonConverter {
	private static final String DM_API_KEY = BuildConfig.DM_API_KEY;
	private static final String URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

	@SerializedName("destination_addresses")
	private List<String> destinationAddresses;
	@SerializedName("origin_addresses")
	private List<String> originAddresses;
	@SerializedName("rows")
	private List<Row> rows;
	@SerializedName("status")
	private String status;

	private DistanceMatrix() {
	}

	public static DistanceMatrix fromJson(String s) {
		return new Gson().fromJson(s, DistanceMatrix.class);
	}

	public static DistanceMatrix fetch(Location startLocation, Location endLocation) {
		String origin = String.format(
				Locale.ENGLISH,
				"%f,%f",
				startLocation.getLatitude(),
				startLocation.getLongitude()
		);
		String destination = String.format(
				Locale.ENGLISH,
				"%f,%f",
				endLocation.getLatitude(),
				endLocation.getLongitude()
		);
		Log.d("DistanceMatrix:", "fetch: (" + origin + ") -> (" + destination + ")");
		return fetch(origin, destination);
	}

	public static DistanceMatrix fetch(String origin, String destination) {
		String uri = String.format(
				"%s?units=%s&origins=%s&destinations=%s&key=%s",
				URL,
				DatabaseContract.UNIT,
				Uri.encode(origin),
				Uri.encode(destination),
				DM_API_KEY
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
			Log.e("DistanceMatrix: ", "fetch: " + e.toString());
		}
		return null;
	}

	/**
	 * Getter methods for class properties.
	 **/
	public String getStatus() {
		return status;
	}

	public List<Row> getRows() {
		return rows;
	}

	public List<String> getOriginAddresses() {
		return originAddresses;
	}

	public String getFirstOriginAddress() {
		return originAddresses.get(0);
	}

	public List<String> getDestinationAddresses() {
		return destinationAddresses;
	}

	public String getFirstDestinationAddress() {
		return destinationAddresses.get(0);
	}

	/**
	 * Getter methods for frequently accessed child class properties.
	 **/
	public long getFirstDistanceValue() {
		return this.getRows().get(0).getElements().get(0).getDistance().getValue();
	}

	public String getFirstDistanceWithUnit() {
		return this.getRows().get(0).getElements().get(0).getDistance().getText();
	}

	public long getFirstDurationValue() {
		return this.getRows().get(0).getElements().get(0).getDuration().getValue();
	}

	public String getFirstDurationWithUnit() {
		return this.getRows().get(0).getElements().get(0).getDuration().getText();
	}

	public String getFirstElementStatus() {
		return this.getRows().get(0).getElements().get(0).getStatus();
	}


	public class Row extends JsonConverter {
		private List<Element> elements;

		public List<Element> getElements() {
			return elements;
		}


		public class Element extends JsonConverter {
			private Distance distance;
			private Duration duration;
			private String status;

			public Distance getDistance() {
				return distance;
			}

			public Duration getDuration() {
				return duration;
			}

			public String getStatus() {
				return status;
			}


			public class Distance extends TextValue {
			}


			public class Duration extends TextValue {
			}


			class TextValue extends JsonConverter {
				private String text;
				private long value;

				public String getText() {
					return text;
				}

				public long getValue() {
					return value;
				}
			}
		}
	}
}