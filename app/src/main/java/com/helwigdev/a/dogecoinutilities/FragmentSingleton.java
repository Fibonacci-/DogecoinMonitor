package com.helwigdev.a.dogecoinutilities;

import android.util.Log;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class FragmentSingleton {
	private static final int CURRENCY_FRAGMENT_POSITION = 0;
	private static final int WALLET_FRAGMENT_POSITION = 1;
	private static final int SETTINGS_FRAGMENT_POSITION = 3;

	private CurrencyFragment mCurrencyFragment;
	private WalletFragment mWalletFragment;
	private SettingsFragment mSettingsFragment;

	private static FragmentSingleton instance = null;
	protected FragmentSingleton() {
		// Exists only to defeat instantiation.
	}
	public static FragmentSingleton get() {
		if(instance == null) {
			instance = new FragmentSingleton();
		}
		return instance;
	}

	public CurrencyFragment getCurrencyFragment() {
		if(mCurrencyFragment == null){
			mCurrencyFragment = CurrencyFragment.newInstance(CURRENCY_FRAGMENT_POSITION + 1);
		}
		return mCurrencyFragment;
	}

	public WalletFragment getWalletFragment() {
		if(mWalletFragment == null){
			mWalletFragment = WalletFragment.newInstance(WALLET_FRAGMENT_POSITION + 1);
		}
		return mWalletFragment;
	}

	public SettingsFragment getSettingsFragment() {
		if(mSettingsFragment == null){
			mSettingsFragment = SettingsFragment.newInstance(SETTINGS_FRAGMENT_POSITION + 1);
		}
		return mSettingsFragment;
	}

	public void invalidate() {
		Log.i("FragmentSingleton","Invalidating all fragments");
		mCurrencyFragment = null;
		mWalletFragment = null;
		mSettingsFragment = null;
	}
}
