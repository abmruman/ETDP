package com.etdp.etdp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
	}

	// Method is called during an upgrade of the database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DatabaseContract.TravelEntry.DELETE_TABLE);
		onCreate(db);
	}
}