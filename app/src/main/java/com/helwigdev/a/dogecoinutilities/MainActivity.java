package com.helwigdev.a.dogecoinutilities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int positionBeforeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                //insert currency fragment
				positionBeforeScan = position;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentHandler.getInstance().getCurrencyFragment(position + 1))
                        .commit();

                break;
            case 1:
                //insert pool fragment
				positionBeforeScan = position;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FragmentHandler.getInstance().getPoolFragment(position + 1))
                        .commit();

                break;
            case 2:
                //hacky bit to re-select the previous nav drawer item
                onNavigationDrawerItemSelected(positionBeforeScan);
                onSectionAttached(positionBeforeScan + 1);
                mNavigationDrawerFragment.setItemPosition(positionBeforeScan);
                //scan new QR and handle result
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
				break;
			case 3:
				//hacky bit to re-select the previous nav drawer item
				onNavigationDrawerItemSelected(positionBeforeScan);
				onSectionAttached(positionBeforeScan + 1);
				mNavigationDrawerFragment.setItemPosition(positionBeforeScan);
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				break;

        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && intent != null) {
            // handle scan result
            Toast.makeText(getApplicationContext(), scanResult.toString(), Toast.LENGTH_SHORT).show();
            Log.d("QR",scanResult.getContents());
			if(IntentIntegrator.QR_CODE_TYPES.contains(scanResult.getFormatName())){
				//could be API key or address
				String[] key = scanResult.getContents().split("\\|");
				for(int i = 0; i < key.length; i++){
					Log.d("Key", i + ": " + key[i]);
				}
			} else if(IntentIntegrator.DATA_MATRIX_TYPES.contains(scanResult.getFormatName())){
				//probably website-generated code to import address
			}

        }
        finishActivity(requestCode);

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
                mTitle = getString(R.string.title_section_pool);
                break;
            case 3:
                mTitle = getString(R.string.title_section_scan);
                break;
			case 4:
				mTitle = getString(R.string.title_section_currency);
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
			//we don't need a menu right now TODO enable this when settings are finished
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

}
