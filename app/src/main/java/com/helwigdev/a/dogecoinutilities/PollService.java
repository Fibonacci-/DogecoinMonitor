package com.helwigdev.a.dogecoinutilities;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Tyler on 3/27/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class PollService extends IntentService {
	public static final String TAG = "PollService";

	public PollService(){
		super(TAG);
	}
	@Override
	protected void onHandleIntent(Intent intent) {

	}
}
