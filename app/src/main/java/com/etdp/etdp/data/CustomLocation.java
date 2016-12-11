package com.etdp.etdp.data;

import android.location.Location;
import android.location.LocationManager;

import com.google.gson.Gson;

public class CustomLocation extends JsonConverter {
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;

	public CustomLocation(double mLatitude, double mLongitude) {
		setLatitude(mLatitude);
		setLongitude(mLongitude);
	}

	public CustomLocation(Location location) {
		setLocation(location);
	}

	public static CustomLocation fromJson(String s) {
		return new Gson().fromJson(s, CustomLocation.class);
	}

	public static Location fromJsonToLocation(String s) {
		return new Gson().fromJson(s, Location.class);
	}

	public static String toJson(Location location) {
		return new Gson().toJson(location);
	}

	public static String toString(Location location) {
		return toJson(location);
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}

	public Location getLocation() {
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(getLatitude());
		location.setLongitude(getLongitude());
		return location;
	}

	public void setLocation(Location location) {
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
	}
}
