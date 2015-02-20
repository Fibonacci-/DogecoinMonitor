package com.helwigdev.a.dogecoinutilities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Tyler on 1/10/2015.
 */
public class SettingsFragment extends PreferenceFragment {
	private static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);
		//simple is as simple does
	}

	public static SettingsFragment newInstance(int sectionNumber) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
}
