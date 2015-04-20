package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends PreferenceActivity {
	//host for SettingsFragment
	protected static final int PURCHASE_ADS_REQUEST_CODE = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("SettingsActivity","Got activity result!");
		if (resultCode != Activity.RESULT_OK) {
			Log.e("SettingsActivity", "Got result code " + resultCode);
			return;
		}
		if (requestCode == PURCHASE_ADS_REQUEST_CODE) {
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

			JSONObject jo;
			try {
				jo = new JSONObject(purchaseData);
				String sku = jo.getString("productId");
				String productId = jo.getString("orderId");
				String devPayload = jo.getString("developerPayload");
				String purchaseToken = jo.getString("purchaseToken");
				if(sku.equals(MainActivity.SKU_REMOVE_ADS)){
					if(!Build.SERIAL.equals(devPayload)){
						Log.e("Billing","Payload and device serial do not match!");
					}
					SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
					prefs.putString(MainActivity.ADS_ORDER_ID, productId);
					prefs.putString(MainActivity.ADS_PURCHASE_TOKEN, purchaseToken);
					prefs.putBoolean(MainActivity.PREF_ADS_REMOVED, true);
					prefs.apply();
					Toast.makeText(this, getResources().getString(R.string.thanks_billing), Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
