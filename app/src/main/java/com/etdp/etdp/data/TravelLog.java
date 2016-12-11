package com.etdp.etdp.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TravelLog {
	public static final SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.ENGLISH);
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH00", Locale.ENGLISH);
	private boolean readonly;
	private DatabaseHelper dbHelper;
	private Long startTime;
	private Long endTime;
	private DistanceMatrix distanceMatrix;

	private CustomLocation originLocation;
	private CustomLocation destLocation;
	private String originAddress;
	private String destAddress;
	private String weather;
	private String day;
	private int time;
	private long distance;
	private long eta;
	private long travelTime;

	/**
	 * Instance for reading from database
	 **/
	public TravelLog(DatabaseHelper dbHelper) throws NullPointerException {
		if (dbHelper == null) {
			throw new NullPointerException();
		}
		this.dbHelper = dbHelper;
		this.readonly = true;
	}

	/**
	 * Instance for writing to database
	 **/
	public TravelLog(
			DatabaseHelper dbHelper,
			Location originLocation,
			Location destLocation,
			DistanceMatrix distanceMatrix,
			Weather weather,
			long startTime,
			long endTime
	) throws NullPointerException {

		this(
				dbHelper,
				new CustomLocation(originLocation),
				new CustomLocation(destLocation),
				distanceMatrix,
				weather,
				startTime,
				endTime
		);
	}

	public TravelLog(
			DatabaseHelper dbHelper,
			CustomLocation originLocation,
			CustomLocation destLocation,
			DistanceMatrix distanceMatrix,
			Weather weather,
			long startTime,
			long endTime
	) throws NullPointerException {

		this(
				dbHelper,
				originLocation,
				destLocation,
				distanceMatrix,
				weather.getRows().get(0).getCondition(),
				startTime,
				endTime
		);
	}

	public TravelLog(
			DatabaseHelper dbHelper,
			CustomLocation originLocation,
			CustomLocation destLocation,
			DistanceMatrix distanceMatrix,
			String weather,
			long startTime,
			long endTime
	) throws NullPointerException {

		if (dbHelper == null
				|| originLocation == null
				|| destLocation == null
				|| distanceMatrix == null) {
			throw new NullPointerException();
		}
		this.dbHelper = dbHelper;

		this.originLocation = originLocation;
		this.destLocation = destLocation;
		this.distanceMatrix = distanceMatrix;
		this.startTime = startTime;
		this.endTime = endTime;

		this.originAddress = distanceMatrix.getFirstOriginAddress();
		this.destAddress = distanceMatrix.getFirstDestinationAddress();
		this.weather = weather;
		this.day = dayFormat.format(this.startTime);
		this.time = Integer.parseInt(timeFormat.format(this.endTime));
		this.distance = distanceMatrix.getFirstDistanceValue();
		this.eta = distanceMatrix.getFirstDurationValue();
		this.travelTime = (endTime - startTime) / 1000;
	}

	public long saveData() {
		if (readonly || originLocation == null || destLocation == null || distanceMatrix == null) {
			return -1;
		}
		synchronized (dbHelper) {
			// Gets the data repository in write mode
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			ContentValues values = getContentValue();

			// Insert the new row, returning the primary key value of the new row
			long newRowId = db.insert(DatabaseContract.TravelEntry.TABLE_NAME, null, values);
			return newRowId;
		}
	}

	private ContentValues getContentValue() {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();

		values.put(
				DatabaseContract.TravelEntry.COLUMN_ORIGIN_LOCATION,
				originLocation.toString()
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_DEST_LOCATION,
				destLocation.toString()
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_ORIGIN_ADDRESS,
				originAddress
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_DEST_ADDRESS,
				destAddress
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_WEATHER,
				weather
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_DAY,
				day
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_TIME,
				time
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_DISTANCE,
				distance
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_ETA,
				eta
		);
		values.put(
				DatabaseContract.TravelEntry.COLUMN_TRAVEL_TIME,
				travelTime
		);
		return values;
	}

	public String toString() {
		return getContentValue().toString();
	}
}
