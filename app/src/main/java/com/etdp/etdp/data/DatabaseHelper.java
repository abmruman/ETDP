package com.etdp.etdp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static DatabaseHelper instance;

	private DatabaseHelper(Context context) {
		super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
	}

	public static synchronized DatabaseHelper getDbHelper(Context context) {
		if (instance == null)
			instance = new DatabaseHelper(context);

		return instance;
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseContract.TravelEntry.CREATE_TABLE);
		if (DatabaseContract.ALLOW_DUMMY_DATA_ON_CREATE) {

			List<TravelLog> travelLogs = new ArrayList<>();
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.796531, 90.389907),
					new CustomLocation(23.794536, 90.405892),
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1700,
					3060,
					751,
					2000)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.796531, 90.389907),
					new CustomLocation(23.794536, 90.405892),
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					900,
					3060,
					751,
					1750)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.796531, 90.389907),
					new CustomLocation(23.794536, 90.405892),
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1500,
					3060,
					751,
					1900)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.796531, 90.389907),
					new CustomLocation(23.794536, 90.405892),
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1400,
					3060,
					751,
					1600)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.796531, 90.389907),
					new CustomLocation(23.794536, 90.405892),
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1900,
					3060,
					751,
					1200)
			);

			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.794536, 90.405892),
					new CustomLocation(23.796531, 90.389907),
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1700,
					3060,
					751,
					2000)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.794536, 90.405892),
					new CustomLocation(23.796531, 90.389907),
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					900,
					3060,
					751,
					1750)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.794536, 90.405892),
					new CustomLocation(23.796531, 90.389907),
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1500,
					3060,
					751,
					1900)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.794536, 90.405892),
					new CustomLocation(23.796531, 90.389907),
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1400,
					3060,
					751,
					1600)
			);
			travelLogs.add(new TravelLog(
					instance,
					new CustomLocation(23.794536, 90.405892),
					new CustomLocation(23.796531, 90.389907),
					"American International University-Bangladesh, Rd No 21, Dhaka, Bangladesh",
					"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh",
					"Haze",
					"Mon",
					1900,
					3060,
					751,
					1200)
			);

			for(TravelLog travelLog : travelLogs){
				Log.d("Database", "onCreate: " + db.insert(DatabaseContract.TravelEntry.TABLE_NAME, null, travelLog.getContentValue()));
			}

		}
	}

	// Method is called during an upgrade of the database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DatabaseContract.TravelEntry.DELETE_TABLE);
		onCreate(db);
	}
}