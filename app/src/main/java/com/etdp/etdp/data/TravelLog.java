package com.etdp.etdp.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TravelLog {
	public static final SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.ENGLISH);
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("H00", Locale.ENGLISH);
	public int _id;
	public CustomLocation originLocation;
	public CustomLocation destLocation;
	public String originAddress;
	public String destAddress;
	public String weather;
	public String day;
	public int time;
	public long distance;
	public long eta;
	public long travelTime;
	private boolean readonly;
	private DatabaseHelper dbHelper;
	private Long startTime;
	private Long endTime;
	private DistanceMatrix distanceMatrix;

	public TravelLog(
			DatabaseHelper dbHelper,
			CustomLocation originLocation,
			CustomLocation destLocation,
			String originAddress,
			String destAddress,
			String weather,
			String day,
			int time,
			long distance,
			long eta,
			long travelTime) {
		this(
				dbHelper,
				-1,
				originLocation,
				destLocation,
				originAddress,
				destAddress,
				weather,
				day,
				time,
				distance,
				eta,
				travelTime
		);
	}

	public TravelLog(
			DatabaseHelper dbHelper,
			int _id,
			CustomLocation originLocation,
			CustomLocation destLocation,
			String originAddress,
			String destAddress,
			String weather,
			String day,
			int time,
			long distance,
			long eta,
			long travelTime) {
		if (_id > -1) this._id = _id;
		this.dbHelper = dbHelper;
		this.originLocation = originLocation;
		this.destLocation = destLocation;
		this.originAddress = originAddress;
		this.destAddress = destAddress;
		this.weather = weather;
		this.day = day;
		this.time = time;
		this.distance = distance;
		this.eta = eta;
		this.travelTime = travelTime;
	}

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
		this.distanceMatrix = distanceMatrix;
		this.startTime = startTime;
		this.endTime = endTime;


		this.originLocation = originLocation;
		this.destLocation = destLocation;
		this.originAddress = distanceMatrix.getFirstOriginAddress();
		this.destAddress = distanceMatrix.getFirstDestinationAddress();
		this.weather = weather;
		this.day = dayFormat.format(this.startTime);
		this.time = Integer.parseInt(timeFormat.format(this.endTime));
		this.distance = distanceMatrix.getFirstDistanceValue();
		this.eta = distanceMatrix.getFirstDurationValue();
		this.travelTime = (endTime - startTime) / 1000;
	}

	public static List<TravelLog> readAllData(DatabaseHelper dbHelper) {
		return readData(dbHelper, null, null, null, null, null);
	}

	public static List<TravelLog> readData(
			DatabaseHelper dbHelper,
			String[] select,
			String where,
			String[] whereArgs,
			String orderBy,
			String limit) {

		List<TravelLog> travelLogs = new ArrayList<>();

		if (dbHelper == null) return null;
		synchronized (dbHelper) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			if (select == null) select = DatabaseContract.TravelEntry.PROJECTION;
			Cursor cursor = db.query(
					DatabaseContract.TravelEntry.TABLE_NAME,
					select,
					where,
					whereArgs,
					null,
					null,
					orderBy,
					limit
			);
			if (cursor.moveToFirst()) {
				do {
					TravelLog travelLog = cursorToTravelLog(dbHelper, cursor);
					travelLogs.add(travelLog);
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();
		}
		return travelLogs;
	}

	public static TravelLog cursorToTravelLog(DatabaseHelper dbHelper, Cursor cursor) {
		return new TravelLog(
				dbHelper,
				CustomLocation.fromJson(cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_ORIGIN_LOCATION))),
				CustomLocation.fromJson(cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_DEST_LOCATION))),
				cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_ORIGIN_ADDRESS)),
				cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_DEST_ADDRESS)),
				cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_WEATHER)),
				cursor.getString(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_DAY)),
				cursor.getInt(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_TIME)),
				cursor.getLong(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_DISTANCE)),
				cursor.getLong(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_ETA)),
				cursor.getLong(cursor.getColumnIndex(DatabaseContract.TravelEntry.COLUMN_TRAVEL_TIME))
		);
	}

	public long saveData() {
		if (readonly || originLocation == null || destLocation == null || distanceMatrix == null) {
			return -1;
		}
		if (dbHelper == null) return -1;

		synchronized (dbHelper) {
			// Gets the data repository in write mode
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			ContentValues values = getContentValue();

			// Insert the new row, returning the primary key value of the new row
			long newRowId = db.insert(DatabaseContract.TravelEntry.TABLE_NAME, null, values);
			db.close();
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
