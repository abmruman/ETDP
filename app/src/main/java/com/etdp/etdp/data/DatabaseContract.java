package com.etdp.etdp.data;

import android.provider.BaseColumns;

public final class DatabaseContract {
	//metric or imperial, this determines the unit for api call
	//Remember, changing this doesn't change the existing values in database
	public static final String UNIT = "metric";
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "etdp.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	/* private prevents from this class to be instantiated */
	private DatabaseContract() {
	}

	/* Inner class defines the table contents */
	public static class TravelEntry implements BaseColumns {
		public static final String TABLE_NAME = "TravelLog";
		public static final String COLUMN_ORIGIN_LOCATION = "originLocation";
		public static final String COLUMN_DEST_LOCATION = "destLocation";
		public static final String COLUMN_ORIGIN_ADDRESS = "originAddress";
		public static final String COLUMN_DEST_ADDRESS = "destAddress";
		public static final String COLUMN_WEATHER = "weather";
		public static final String COLUMN_DAY = "day";
		public static final String COLUMN_TIME = "time";
		public static final String COLUMN_DISTANCE = "distance";
		public static final String COLUMN_ETA = "ETA";
		public static final String COLUMN_TRAVEL_TIME = "travelTime";

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TABLE_NAME + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
				COLUMN_ORIGIN_LOCATION + TEXT_TYPE + COMMA_SEP +
				COLUMN_DEST_LOCATION + TEXT_TYPE + COMMA_SEP +
				COLUMN_ORIGIN_ADDRESS + TEXT_TYPE + COMMA_SEP +
				COLUMN_DEST_ADDRESS + TEXT_TYPE + COMMA_SEP +
				COLUMN_WEATHER + TEXT_TYPE + COMMA_SEP +
				COLUMN_DAY + TEXT_TYPE + COMMA_SEP +
				COLUMN_TIME + INTEGER_TYPE + COMMA_SEP +
				COLUMN_DISTANCE + INTEGER_TYPE + COMMA_SEP +
				COLUMN_ETA + INTEGER_TYPE + COMMA_SEP +
				COLUMN_TRAVEL_TIME + INTEGER_TYPE + " )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

		public static final String[] PROJECTION = {
				_ID,
				COLUMN_ORIGIN_LOCATION,
				COLUMN_DEST_LOCATION,
				COLUMN_ORIGIN_ADDRESS,
				COLUMN_DEST_ADDRESS,
				COLUMN_WEATHER,
				COLUMN_DAY,
				COLUMN_TIME,
				COLUMN_DISTANCE,
				COLUMN_ETA,
				COLUMN_TRAVEL_TIME
		};
	}
}
