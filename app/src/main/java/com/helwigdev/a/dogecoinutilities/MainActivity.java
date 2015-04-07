package com.helwigdev.a.dogecoinutilities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	//declarations

	protected static final String TAG = "MainActivity";

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	public NavigationDrawerFragment getNavigationDrawerFragment() {
		return mNavigationDrawerFragment;
	}

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private int positionBeforeScan;
	AdView mAdView;
	static FragmentSingleton mFragmentSingleton;

	private int rotationBeforeScan = Surface.ROTATION_0;

	//billing setup
	IInAppBillingService mService;
	public static final String SKU_REMOVE_ADS = "donate_remove_ads";


	ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name,
									   IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
			try {
				Log.e(TAG, "Getting purchase list");
				Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
				int response = ownedItems.getInt("RESPONSE_CODE");

				if (response == 0) {
					ArrayList<String> ownedSkus =
							ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					ArrayList<String> purchaseDataList =
							ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					ArrayList<String> signatureList =
							ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
					String continuationToken =
							ownedItems.getString("INAPP_CONTINUATION_TOKEN");

					for (int i = 0; i < purchaseDataList.size(); ++i) {
						String purchaseData = purchaseDataList.get(i);
						String signature = signatureList.get(i);
						String sku = ownedSkus.get(i);
						if(sku.equalsIgnoreCase(SKU_REMOVE_ADS)){
							//TODO disable ads
						}

					}

					// if continuationToken != null, this would be where we check for more purchases
					//however there is only one available at this point
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);//init preferences, if it hasn't been done yet

		//setup billing
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);



		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstStart", true)) {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstStart",
					false).commit();
			//show first start dialog
			DialogFragment newFragment = FirstAlertDialog.newInstance(
					R.string.hello);
			newFragment.show(getFragmentManager(), "dialog");


		}

		//set up views
		//nav drawer fragment will handle autolaunching the first fragment
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_main);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		mAdView = (AdView) findViewById(R.id.ad_main);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("827EB1A5D0932A3128F6670540C5EFEC")
				.addTestDevice("870856BEB78EBA07DB2D4697C70F2369")
				.build();
		mAdView.loadAd(adRequest);

		mFragmentSingleton = FragmentSingleton.get(this);
	}

	@Override
	protected void onPause() {
		mAdView.pause();
		mFragmentSingleton.saveData();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
	}

	@Override
	protected void onDestroy() {
		mAdView.destroy();
		if (isFinishing()) {
			mFragmentSingleton.invalidate();
		}
		if (mService != null) {
			unbindService(mServiceConn);
		}
		super.onDestroy();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		if (mFragmentSingleton == null) {
			mFragmentSingleton = FragmentSingleton.get(this);
		}
		switch (position) {
			case 0:
				//insert currency fragment
				positionBeforeScan = position;
				fragmentManager.beginTransaction()
						.replace(R.id.container, mFragmentSingleton.getCurrencyFragment())
						.commit();
				onSectionAttached(position + 1);
				break;
			case 1:
				//insert pool fragment
				positionBeforeScan = position;
				fragmentManager.beginTransaction()
						.replace(R.id.container, mFragmentSingleton.getWalletFragment())
						.commit();
				onSectionAttached(position + 1);
				break;
			case 2:
				//hacky bit to re-select the previous nav drawer item
				onNavigationDrawerItemSelected(positionBeforeScan);
				onSectionAttached(positionBeforeScan + 1);
				mNavigationDrawerFragment.setItemPosition(positionBeforeScan);

				rotationBeforeScan = getWindowManager().getDefaultDisplay().getRotation();

				//scan new QR and handle result
				IntentIntegrator integrator = new IntentIntegrator(this);
				integrator.initiateScan();
				break;
			case 3:
				//same hacky bit
				//TODO investigate why this will always re-select position 0 instead of dynamically switching between the first two
				onNavigationDrawerItemSelected(positionBeforeScan);
				onSectionAttached(positionBeforeScan + 1);
				mNavigationDrawerFragment.setItemPosition(positionBeforeScan);

				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				break;

		}


	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		//if the device is on a flat surface, it will not switch back into portrait mode after scanning a qr code
		//this fixes that issue
		//TODO investigate if this will force the activity into portrait mode for the remainder of the activity lifecycle
//		if (rotationBeforeScan == 0) {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		}
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
					resultCode, intent);

			if (scanResult != null && intent != null) {

				// handle scan result
				Log.d("QR", scanResult.getContents());
				new VerifyAndAdd(Utilities.checkQRCodeType(scanResult.getContents())).execute
						(scanResult.getContents());

			}
			finishActivity(requestCode);
		}
		//hacky bit to get around previous hacky bit to get around the fact that Android doesn't
		// like launching intents from a nav drawer
		if (mNavigationDrawerFragment.isDrawerOpen()) {
			DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerLayout.closeDrawers();
		}
	}

	public void onSectionAttached(int number) {
		//update activity title

		switch (number) {
			case 1:
				mTitle = getString(R.string.title_section_currency);
				break;
			case 2:
				mTitle = getString(R.string.title_section_wallet);
				break;
			case 3:
				mTitle = getString(R.string.title_section_add_wallet);
				break;
			case 4:
				mTitle = getString(R.string.title_section_settings);
				break;

		}
	}

	public void restoreActionBar() {
		//reset actionbar with appropriate stylings
		ActionBar actionBar = getActionBar();
		if (actionBar == null) {
			Log.e("ACTIONBAR", "Actionbar not found");
			return;
		}
		if (Build.VERSION.SDK_INT != 21) {
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.amber_500));
		}
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			//getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();


		return id == R.id.action_settings || super.onOptionsItemSelected(item);

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */

	//TODO we should be able to remove this soon - keep as example fragment for a little more
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(
					getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}

	//This task verifies that addresses are valid and adds them to the DB
	public class VerifyAndAdd extends AsyncTask<String, Void, Void> {
		//constructor with type already defined
		public VerifyAndAdd(int type) {
			mType = type;
		}

		int mType;

		//extract address data from various types and check each address before adding
		@Override
		protected Void doInBackground(String... params) {

			switch (mType) {
				case Utilities.QR_TYPE_CLIENT_WALLET_URI:
					Log.i(TAG, "Unrecognized Wallet URI");
					for (String s : params) {
						addWalletAddress(s.replace("dogecoin:", ""));
					}
					break;

				case Utilities.QR_TYPE_JSON_ADDRESSES:
					Log.i(TAG, "Got type JSON addresses");
					try {
						//should be an array
						JSONArray array = new JSONArray(params[0]);
						for (int i = 0; i < array.length(); i++) {
							JSONObject o = array.getJSONObject(i);
							addWalletAddress(o.getString("address"));
						}
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
					break;

				case Utilities.QR_TYPE_WALLET_ADDRESS:
					Log.i(TAG, "Got type Wallet Address");
					for (String s : params) {
						addWalletAddress(s);
					}
					break;

				case Utilities.QR_TYPE_POOL_API_KEY:
					Log.i(TAG, "Got type Pool API key");
					for (String s : params) {
						addPool(s);
					}
					break;
				case Utilities.QR_TYPE_UNKNOWN:
					Log.i(TAG, "Unrecognized QR type");
					break;
				default:
					Log.e(TAG, "VerifyAndAdd called incorrectly: aborting");
					break;
			}
			return null;
		}

		/**
		 * NOT FOR USE ON MAIN THREAD
		 * <p/>
		 * NETWORK ACTIVITY IS DONE IN THIS METHOD
		 * <p/>
		 * Verifies a given Dogecoin address and adds it to the list, if it is valid
		 *
		 * @param address The address to add
		 */
		private void addWalletAddress(String address) {
			String url = "https://dogechain.info/chain/Dogecoin/q/checkaddress/" + address;

			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.connect();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e(TAG, "Invalid address: response is " + connection.getResponseCode());
					return;
				}

				mFragmentSingleton.addWallet(address);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * NOT FOR USE ON MAIN THREAD
		 * <p/>
		 * NETWORK ACTIVITY IS DONE IN THIS METHOD
		 * <p/>
		 * Verifies a given Dogecoin address and adds it to the list, if it is valid
		 *
		 * @param apiData The pool API MPOS string to add
		 */
		private void addPool(String apiData) {

		}


	}

}
