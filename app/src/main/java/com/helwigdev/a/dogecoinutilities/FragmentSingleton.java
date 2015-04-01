package com.helwigdev.a.dogecoinutilities;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class FragmentSingleton {
	private static final String TAG = "FragmentSingleton";

	private static final int CURRENCY_FRAGMENT_POSITION = 0;
	private static final int WALLET_FRAGMENT_POSITION = 1;
	private static final int SETTINGS_FRAGMENT_POSITION = 3;

	private CurrencyFragment mCurrencyFragment;
	private WalletFragment mWalletFragment;
	private SettingsFragment mSettingsFragment;
	private DatabaseHelper mHelper;

	private ArrayList<String> mPoolList;

	private Context mContext;

	private static FragmentSingleton instance = null;

	protected FragmentSingleton(Context c) {
		mContext = c;
		mHelper = new DatabaseHelper(mContext);
		reloadData();
	}

	public void reloadData() {
		ArrayList<String> addressList;
		mPoolList = new ArrayList<>();

		try {
			mPoolList = Utilities.getPoolApiList(mContext);
			addressList = Utilities.getWalletAddressList(mContext);

			//convert old custom storage mechanism to new shiny DB storage
			if (addressList.size() != 0) {
				for (String s : addressList) {
					mHelper.insertWallet(s);
					Log.i(TAG, "Converting wallet storage to new format... ");
				}
				Utilities.writeAllWalletList(mContext, new ArrayList<String>());//erase old db
				// files
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not load data from storage: " + e.toString());
		}
	}

	public static FragmentSingleton get(Context c) {
		//#justSingletonThings
		if (instance == null) {
			instance = new FragmentSingleton(c);
		}
		return instance;
	}

	//the rest of this class is simple getters/setters

	public DatabaseHelper getHelper() {
		if (mHelper == null) {
			mHelper = new DatabaseHelper(mContext);
		}
		return mHelper;
	}

	public ArrayList<String> getPoolList() {
		return mPoolList;
	}

	public ArrayList<DatabaseHelper.TimedWallet> queryAmounts(String address) {
		return mHelper.queryAmounts(address);
	}

	public ArrayList<String> getAddressList() {
		return mHelper.queryAddresses();
	}

	public void addWallet(String address) {

		mHelper.insertWallet(address);

	}

	public void addAmount(String address, float balance){
		mHelper.insertAmount(address, balance);
	}

	public void addPool(String apiString) {
		mPoolList.add(apiString);
	}

	public CurrencyFragment getCurrencyFragment() {
		if (mCurrencyFragment == null) {
			mCurrencyFragment = CurrencyFragment.newInstance(CURRENCY_FRAGMENT_POSITION + 1);
		}
		return mCurrencyFragment;
	}

	public WalletFragment getWalletFragment() {
		if (mWalletFragment == null) {
			mWalletFragment = WalletFragment.newInstance(WALLET_FRAGMENT_POSITION + 1);
		}
		return mWalletFragment;
	}

	public SettingsFragment getSettingsFragment() {
		if (mSettingsFragment == null) {
			mSettingsFragment = SettingsFragment.newInstance(SETTINGS_FRAGMENT_POSITION + 1);
		}
		return mSettingsFragment;
	}

	public void invalidate() {
		Log.i("FragmentSingleton", "Invalidating all fragments");
		mCurrencyFragment = null;
		mWalletFragment = null;
		mSettingsFragment = null;


	}

	public void saveData() {
		Log.i(TAG, "Saving data to disk");
		Utilities.writeAllPoolApiList(mContext, mPoolList);
	}
}
