package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 * This fragment retrieves and shows current satoshi and fiat values for Dogecoin.
 */
public class CurrencyFragment extends Fragment {
	//declarations
	private static final String ARG_SECTION_NUMBER = "section_number";
	public static final String PREF_LOCAL_CURRENCY = "local_currency";
	private static final String TAG = "CurrencyFragment";
	private ProgressBar pbSat;
	private ProgressBar pbFiat;
	private TextView tvSatSub1;
	private TextView tvSatSub2;
	private TextView tvFiatTitle;
	private TextView tvFiatSub1;
	private TextView tvFiatSub2;
	private FragmentSingleton mFragmentSingleton;

	private boolean isLaunching;

	ProgressDialog progress;

	private Menu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//make sure menu inflates properly
		setHasOptionsMenu(true);
		//if first launch of fragment
		isLaunching = savedInstanceState != null;
		isLaunching = !isLaunching;
		mFragmentSingleton = FragmentSingleton.get(getActivity());
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_currency, container, false);

		CardView cvSat = (CardView) v.findViewById(R.id.card_view_sat);
//		cvSat.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				ArrayList<Object[]> satList = mFragmentSingleton.getHelper().getBaseValues("BTC");
//				for(Object[] d : satList){
//					d[0] = (double) d[0] * 100000000;//convert to sat
//				}
//				DataPoint[] points = new DataPoint[satList.size()];
//				for(int i = 0; i < satList.size(); i++){
//					Object[] d = satList.get(i);
//					points[i] = new DataPoint(new Date((long) d[1]), (double)d[0]);
//				}
//				LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
//				GraphDialog dialog = GraphDialog.newInstance(R.string.hello);
//				ArrayList<LineGraphSeries<DataPoint>> seriesArray = new ArrayList<LineGraphSeries<DataPoint>>();
//				seriesArray.add(series);
//				dialog.setSeriesArray(seriesArray);
//				dialog.show(getFragmentManager(), "graph");
//			}
//		});
//		CardView cvFiat = (CardView) v.findViewById(R.id.card_view_fiat);
//		cvFiat.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String local = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PREF_LOCAL_CURRENCY, "USD");
//				ArrayList<Object[]> fiatList = mFragmentSingleton.getHelper().getBaseValues(local);
//			}
//		});
		//link to xml
		pbSat = (ProgressBar) v.findViewById(R.id.prog_sat);
		tvSatSub1 = (TextView) v.findViewById(R.id.tv_frag_sub_satoshi_1);
		tvSatSub2 = (TextView) v.findViewById(R.id.tv_frag_sub_satoshi_2);

		pbFiat = (ProgressBar) v.findViewById(R.id.prog_fiat);
		tvFiatTitle = (TextView) v.findViewById(R.id.tv_frag_header_fiat);
		tvFiatSub1 = (TextView) v.findViewById(R.id.tv_frag_sub_fiat_1);

		tvFiatSub2 = (TextView) v.findViewById(R.id.tv_frag_sub_fiat_2);
		//we don't want to refresh data each time the view is drawn
		//so we check for first launch in onCreate
		//then refresh page data if we don't have it yet
		//note this would also be possible by setting and clearing preference values
		//but that method would become buggy if the app force closed
		//and didn't properly clear preference values
		if (isLaunching) {//first launch
			//note that we can't use refreshData() here
			//there is no guarantee that the menu has been inflated yet
			new GetCurrencyAsync().execute(tvSatSub2);
			new GetCurrencyAsync().execute(tvFiatSub2);
			isLaunching = false;
		}

		return v;
	}

	public static CurrencyFragment newInstance(int sectionNumber) {
		//nav drawer selection
		CurrencyFragment fragment = new CurrencyFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(
				getArguments().getInt(ARG_SECTION_NUMBER));
	}

	private void refreshData() {
		//set refresh icon to indeterminate progressbar
		MenuItem itemRefresh = mMenu.findItem(R.id.menu_currency_refresh);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		View abprogress = inflater.inflate(R.layout.progress_wheel, null);
		itemRefresh.setActionView(abprogress);

		//swap out information views for progressbars
		pbSat.setVisibility(View.VISIBLE);
		pbFiat.setVisibility(View.VISIBLE);
		tvSatSub1.setVisibility(View.GONE);
		tvSatSub2.setVisibility(View.GONE);
		tvFiatSub1.setVisibility(View.GONE);
		tvFiatSub2.setVisibility(View.GONE);

		//start network threads
		new GetCurrencyAsync().execute(tvSatSub2);
		new GetCurrencyAsync().execute(tvFiatSub2);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//standard menu inflation with nav drawer
		if (!((MainActivity) getActivity()).getNavigationDrawerFragment().isDrawerOpen()) {
			inflater.inflate(R.menu.currency, menu);
			mMenu = menu;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_currency_refresh:
				refreshData();
				return true;
			default:
				//take no action
				return super.onOptionsItemSelected(item);
		}
	}

	public class GetCurrencyAsync extends AsyncTask<TextView, Void, String> {

		//here we have the meat of the fragment

		private String urlGetDogeSat = "https://block" +
				".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=BTC";
		private String urlGetDogeLocal;//set in preExecute
		TextView tv;

		@Override
		protected void onPreExecute() {
			//get local currency, if it has been set
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String local = prefs.getString(PREF_LOCAL_CURRENCY, "USD");
			//build API url for local currency
			urlGetDogeLocal = "https://block" +
					".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=" +
					local;
		}

		@Override
		protected String doInBackground(TextView... params) {
			if (params[0]==null) return getResources().getString(R.string.error);
			tv = params[0];
			String urlToGet;
			if (tv == tvSatSub2) {
				urlToGet = urlGetDogeSat;
			} else {
				urlToGet = urlGetDogeLocal;
			}

			try {
				String s = new String(getUrlBytes(urlToGet));//do network get
				//we have webdata, now to convert to JSON and parse
				JSONObject object = new JSONObject(s);
				if (object.getString("status").equals("success")) {//otherwise, no recent trades found or api error
					JSONObject data = object.getJSONObject("data");
					JSONArray priceArray = data.getJSONArray("prices");

					if (priceArray.length() == 0) {
						//no trades found
						//sometimes the api returns success but without any price data
						//it's very odd
						return getResources().getString(R.string.no_trades_found);
					} else {
						//build string
						s = "";
						for (int i = 0; i < priceArray.length(); i++) {
							JSONObject o = priceArray.getJSONObject(i);
							double price;
							if (tv == tvSatSub2) {
								price = o.getDouble("price") * 100000000;//satoshi is measured as: 1Doge = n BTC * 10^8
								int intPrice = (int) Math.round(price);
								s += intPrice + " on " + o.getString("exchange");
								s += Utilities.newLine;
							} else {
								price = o.getDouble("price") * 1000;
								s += String.format("%.5f", price) + " on " + o.getString("exchange");//account for silly double multiplication
								s += Utilities.newLine;
							}

						}
						return s;
					}

				} else {
					//nondescript api fail
					return getResources().getString(R.string.api_fail);
				}

			} catch (ConnectException e) {
				return getResources().getString(R.string.network_fail);
			} catch (UnknownHostException e) {
				return getResources().getString(R.string.no_network);
			} catch (Exception e) {
				e.printStackTrace();
				return getResources().getString(R.string.error);
			}

		}


		@Override
		protected void onPostExecute(final String s) {
			//this section of the asynctask is run on the UI thread
			//so we can touch and modify the UI
			if (isAdded()) {//don't touch it if the fragment isn't attached to an activity
				if (s != null) {//null return means error, don't modify
					//set appropriate values to appropriate textviews
					//s will already be formatted properly
					if (tv == tvSatSub2) {
						pbSat.setVisibility(View.GONE);
						tvSatSub1.setVisibility(View.VISIBLE);
						tvSatSub2.setVisibility(View.VISIBLE);
						tvSatSub1.setText(getResources().getString(R.string.trading_at));
						tvSatSub2.setText(s);
					} else if (tv == tvFiatSub2) {
						pbFiat.setVisibility(View.GONE);
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
								(getActivity());
						String local = prefs.getString(PREF_LOCAL_CURRENCY, "USD");
						tvFiatSub1.setVisibility(View.VISIBLE);
						tvFiatSub2.setVisibility(View.VISIBLE);
						tvFiatTitle.setText(local);
						tvFiatSub1.setText(getResources().getString(R.string.worth_1000));
						tvFiatSub2.setText(s);
					}
				}
				if (mMenu != null) {
					//reset menu item to previous icon
					MenuItem itemRefresh = mMenu.findItem(R.id.menu_currency_refresh);
					if (itemRefresh != null) itemRefresh.setActionView(null);
				}
			}
		}

		//read data from network connection
		byte[] getUrlBytes(String urlSpec) throws IOException {
			URL url = new URL(urlSpec);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();//can cast - will never be not a HttpURLConnection

			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = connection.getInputStream();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {//some network issue
					Log.e(CurrencyFragment.TAG, connection.getResponseMessage() + " : " + connection
							.getResponseCode());
					return null;
				}

				int bytesRead = 0;
				byte[] buffer = new byte[1024];
				while ((bytesRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, bytesRead);//read data stream
				}
				out.close();
				return out.toByteArray();
			} finally {
				connection.disconnect();
			}
		}
	}

}
