package com.helwigdev.a.dogecoinutilities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by Tyler on 3/27/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class PollService extends IntentService {
	public static final String TAG = "PollService";

	public PollService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG,"Got intent!");
		//TODO poll for all wallet amounts, conversion rates, and sat rates when graphs are implemented
		//check for network
		//include legacy network check
		ConnectivityManager cm = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
				cm.getActiveNetworkInfo() != null;
		if (!isNetworkAvailable) return;


	}
}
