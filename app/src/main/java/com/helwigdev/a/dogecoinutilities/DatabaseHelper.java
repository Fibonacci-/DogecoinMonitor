package com.helwigdev.a.dogecoinutilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tyler on 3/23/2015.
 * All code herein copyright Helwig Development 3/23/2015
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "data.sqlite";
	private static final int VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
