package com.helwigdev.a.dogecoinutilities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	protected static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

	public NavigationDrawerFragment getNavigationDrawerFragment(){
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//TODO add welcome dialog

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

		mAdView = (AdView) findViewById(R.id.ad_main);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("827EB1A5D0932A3128F6670540C5EFEC")
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
		if(isFinishing()) {
			mFragmentSingleton.invalidate();
		}
		super.onDestroy();
	}

	@Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
		if(mFragmentSingleton == null){
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

				//insert settings fragment
				positionBeforeScan = position;
				fragmentManager.beginTransaction()
						.replace(R.id.container, mFragmentSingleton.getSettingsFragment())
						.commit();
				onSectionAttached(position + 1);
				break;

        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(rotationBeforeScan == 0){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		if(resultCode != Activity.RESULT_OK) return;
		if(requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

			if (scanResult != null && intent != null) {

				// handle scan result
				Toast.makeText(getApplicationContext(), scanResult.toString(), Toast.LENGTH_SHORT).show();
				Log.d("QR", scanResult.getContents());
				new VerifyAndAdd(Utilities.checkQRCodeType(scanResult.getContents())).execute(scanResult.getContents());

			}
			finishActivity(requestCode);
		}
        //hacky bit to get around previous hacky bit to get around the fact that Android doesn't like launching intents from a nav drawer
        if(mNavigationDrawerFragment.isDrawerOpen()){
            DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerLayout.closeDrawers();
        }
    }

    public void onSectionAttached(int number) {
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
        ActionBar actionBar = getActionBar();
		if(actionBar == null){
			Log.e("ACTIONBAR","Actionbar not found");
			return;
		}
		if(Build.VERSION.SDK_INT != 21) {
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
            getMenuInflater().inflate(R.menu.main, menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
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

	public class VerifyAndAdd extends AsyncTask<String, Void, Void>{
		public VerifyAndAdd(int type){
			mType = type;
		}

		int mType;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {

			switch (mType){
				case Utilities.QR_TYPE_CLIENT_WALLET_URI:
					Log.i(TAG, "Unrecognized Wallet URI");
					for(String s : params){
						addWalletAddress(s.replace("dogecoin:", ""));
					}
					break;

				case Utilities.QR_TYPE_JSON_ADDRESSES:
					Log.i(TAG, "Got type JSON addresses");
					try{
						//should be an array
						JSONArray array = new JSONArray(params[0]);
						for(int i = 0; i < array.length(); i++){
							JSONObject o = array.getJSONObject(i);
							addWalletAddress(o.getString("address"));
						}
					} catch (Exception e){
						Log.e(TAG, e.toString());
					}
					break;

				case Utilities.QR_TYPE_WALLET_ADDRESS:
					Log.i(TAG, "Got type Wallet Address");
					for(String s : params){
						addWalletAddress(s);
					}
					break;

				case Utilities.QR_TYPE_POOL_API_KEY:
					Log.i(TAG, "Got type Pool API key");
					for(String s : params){
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
		 *
		 * NETWORK ACTIVITY IS DONE IN THIS METHOD
		 *
		 * Verifies a given Dogecoin address and adds it to the list, if it is valid
		 *
		 * @param address The address to add
		 */
		private void addWalletAddress(String address) {
			String url = "https://dogechain.info/chain/Dogecoin/q/checkaddress/" + address;

			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.connect();
				if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
					Log.e(TAG, "Invalid address: response is " + connection.getResponseCode());
					return;
				}

				if(!mFragmentSingleton.addWallet(address)){
					//it's already been added
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), "Address has already been added!", Toast.LENGTH_SHORT).show();
						}
					});
				}
				Log.i(TAG, "Address is OK: " + address);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * NOT FOR USE ON MAIN THREAD
		 *
		 * NETWORK ACTIVITY IS DONE IN THIS METHOD
		 *
		 * Verifies a given Dogecoin address and adds it to the list, if it is valid
		 *
		 * @param apiData The pool API MPOS string to add
		 */
		private void addPool(String apiData){

		}


	}

}
