package com.helwigdev.a.dogecoinutilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import de.langerhans.wallet.integration.android.BitcoinIntegration;

/**
 * Created by Tyler on 1/10/2015.
 * Copyright 2015 by Helwig Development
 */
public class SettingsFragment extends PreferenceFragment {
	//simple PreferenceFragment host
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String BACKUP_FILE_NAME = "dogecoin_backup.txt";
	private static final String TAG = "SettingsFragment";
	public static final String DONATE_ADDRESS = "DBeTGY7wuEvL17MPddbGNX9FyFkhWGS1pQ";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);
		//simple is as simple does
		Preference pref_backup = findPreference("backup");
		pref_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//get list of wallets and format
				ArrayList<String> walletList = FragmentSingleton.get(getActivity()).getAddressList();
				String toWrite = Utilities.backupFormat(walletList);
				try {
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
						//if we can read and write to the sd card
						File output = new File(Environment.getExternalStorageDirectory(), BACKUP_FILE_NAME);

						FileOutputStream fos = new FileOutputStream(output);
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
						writer.write(toWrite);
						writer.flush();
						writer.close();
						Toast.makeText(getActivity(), "Backup written to " + output.getAbsolutePath(), Toast.LENGTH_SHORT).show();
						return true;
					} else {
						throw new Exception("Could not write to external storage: not writable");
					}
				} catch (Exception e) {
					Log.e(TAG, "Could not write to backup file: " + e);
					Toast.makeText(getActivity(), "Could not write backup file to external storage!" +
							"\nIs your phone plugged into a computer?", Toast.LENGTH_SHORT).show();
					return false;
				}

			}
		});

		Preference pref_restore = findPreference("restore");
		pref_restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try{
					File input = new File(Environment.getExternalStorageDirectory(), BACKUP_FILE_NAME);
					if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
							Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())){
						//can read

						BufferedReader br = new BufferedReader(new FileReader(input));
						//pool things are legacy - I'm putting them in now (4/9/15) in case we have
						//to re-implement pools at a later date
						int numPools = Integer.parseInt(br.readLine());//will always be 0
						ArrayList<String> poolList = new ArrayList<String>();
						for (int i = 0; i < numPools; i++) {
							poolList.add(br.readLine());
						}
						int numWallets = Integer.parseInt(br.readLine());
						ArrayList<String> walletList = new ArrayList<String>();
						for (int i = 0; i < numWallets; i++) {
							walletList.add(br.readLine());
						}
						br.close();
						FragmentSingleton fs = FragmentSingleton.get(getActivity());
						for(String s : walletList){
							//since this is from a backup, all wallets should already have been checked against the api
							//if someone wants to break the app, they can go for it, I'll give them the option
							fs.addWallet(s);
						}
						return true;
					} else {
						throw new Exception("Can't read from sdcard: external storage not readable");
					}
				} catch (Exception e){
					Log.e(TAG, "Could not read backup file: " + e);
					Toast.makeText(getActivity(), "Could not restore backup!", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});

		Preference donate_ads = findPreference("donateBilling");
		Preference donate_doge = findPreference("donateDoge");

		donate_ads.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//TODO start billing for sku
				return false;
			}
		});
		donate_doge.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//start intent for doge wallet
				long oneHundredDoge = 100 * 100000000l;
				//request was designed for satoshi amounts, so we need to multiply
				//amount by a lot
				BitcoinIntegration.request(getActivity(), DONATE_ADDRESS, oneHundredDoge);
				return false;
			}
		});

	}


	public static SettingsFragment newInstance(int sectionNumber) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
