package com.helwigdev.a.dogecoinutilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tyler on 3/23/2015.
 * All code herein copyright Helwig Development 3/23/2015
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String TAG = "DatabaseHelper";

	private static final String DB_NAME = "data.sqlite";
	private static final int VERSION = 1;
	/*
	Okay, let's plan out this DB.
	Will need:
	Table of wallets: _id pk ai, address
	Table of amounts: wallet_id, amount
	Table of conversion rates: _id (timestamp) pk, currency, rate
		All conversions will be Doge -> Currency
	 */

	private static final String TABLE_WALLET = "wallet";
	private static final String COLUMN_WALLET_ID = "_id";
	private static final String COLUMN_WALLET_ADDRESS = "address";

	private static final String TABLE_AMOUNT = "amount";
	private static final String COLUMN_AMOUNT_WALLET_ID = "wallet_id";
	private static final String COLUMN_AMOUNT_AMOUNT = "amount_amount";
	private static final String COLUMN_AMOUNT_TIMESTAMP = "timestamp";

	private static final String TABLE_CONVERSION_RATE = "conversion_rate";
	private static final String COLUMN_CONVERSION_RATE_ID = "_id";
	private static final String COLUMN_CONVERSION_RATE_TIMESTAMP = "timestamp";
	private static final String COLUMN_CONVERSION_RATE_BASE_CURRENCY = "base";
	private static final String COLUMN_CONVERSION_RATE_RATE = "rate";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//create table wallet
		db.execSQL("create table wallet (" +
				"_id integer primary key autoincrement, address varchar(100))");

		//create table amount
		db.execSQL("create table amount (" +
				" amount real, wallet_id integer references wallet(_id), timestamp integer)");

		//create table conversion rates
		db.execSQL("create table conversion_rate (" +
				"_id integer primary key autoincrement, timestamp integer, base varchar(10), rate real)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//implement changes here if we ever change the DB
	}

	public long insertAmount(String address, float amount){
		//need to get wallet ID first
		long walletId = findWalletId(address);
		if(walletId != -1){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_AMOUNT_AMOUNT, amount);
			cv.put(COLUMN_AMOUNT_WALLET_ID, walletId);
			cv.put(COLUMN_AMOUNT_TIMESTAMP, new Date().getTime());
			Log.i(TAG, "Inserting amount for wallet");
			return getWritableDatabase().insert(TABLE_AMOUNT, null, cv);
		}
		return -1;
	}

	private long findWalletId(String address){
		Cursor cursor = getReadableDatabase().query(TABLE_WALLET,
				null,
				null,
				null,
				null,null,null);
		cursor.moveToFirst();
		if(cursor.isBeforeFirst() || cursor.isAfterLast()) return -1;//if cursor is not empty

			while(!cursor.isAfterLast()){//step through cursor to check if we have this address saved
				String savedAddress = cursor.getString(cursor.getColumnIndex(COLUMN_WALLET_ADDRESS));
				if(address.equals(savedAddress)){
					return cursor.getLong(cursor.getColumnIndex(COLUMN_WALLET_ID));//if we already saved this address, return the row ID
				}
				cursor.moveToNext();
			}
		return -1;
	}

	public long insertWallet(String address){
		Cursor cursor = getReadableDatabase().query(TABLE_WALLET,
				null,
				null,
				null,
				null,null,null);
		cursor.moveToFirst();
		if(!(cursor.isBeforeFirst() || cursor.isAfterLast())){//if cursor is not empty

			while(!cursor.isAfterLast()){//step through cursor to check if we have this address saved
				String savedAddress = cursor.getString(cursor.getColumnIndex(COLUMN_WALLET_ADDRESS));
				if(address.equals(savedAddress)){
					Log.e(TAG, "Address already added: " + address);
					return cursor.getLong(cursor.getColumnIndex(COLUMN_WALLET_ID));//if we already saved this address, return the row ID
				}
				cursor.moveToNext();
			}
		}

		ContentValues cv = new ContentValues();
		cv.put(COLUMN_WALLET_ADDRESS, address);
		Log.i(TAG, "Inserting wallet address " + address);
		return getWritableDatabase().insert(TABLE_WALLET, null, cv);
	}

	public ArrayList<String> queryAddresses(){
		Log.e(TAG, "Querying addresses");
		Cursor cursor = getReadableDatabase().query(TABLE_WALLET,
				null,
				null,
				null,
				null,null,null);
		cursor.moveToFirst();
		if(cursor.isBeforeFirst() || cursor.isAfterLast()){
			Log.i(TAG, "Empty cursor!");
			return new ArrayList<>();//return empty arraylist
		}


		ArrayList<String> list = new ArrayList<>();
		while(!cursor.isAfterLast()){
			list.add(cursor.getString(cursor.getColumnIndex(COLUMN_WALLET_ADDRESS)));
			Log.i(TAG, "Found DB address: " + cursor.getString(cursor.getColumnIndex(COLUMN_WALLET_ADDRESS)));
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

}
