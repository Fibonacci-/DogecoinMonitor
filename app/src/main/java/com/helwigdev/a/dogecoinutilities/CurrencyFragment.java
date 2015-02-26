package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class CurrencyFragment extends Fragment {
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String PREF_LOCAL_CURRENCY = "local_currency";
	private ProgressBar pbSat;
	private ProgressBar pbFiat;
	private TextView tvSatSub1;
	private TextView tvSatSub2;
	private TextView tvFiatTitle;
	private TextView tvFiatSub1;
	private TextView tvFiatSub2;

	private boolean isLaunching;

	private Menu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		isLaunching = savedInstanceState != null;
		isLaunching = !isLaunching;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_currency, container, false);

		pbSat = (ProgressBar) v.findViewById(R.id.prog_sat);
		tvSatSub1 = (TextView) v.findViewById(R.id.tv_frag_sub_satoshi_1);
		tvSatSub2 = (TextView) v.findViewById(R.id.tv_frag_sub_satoshi_2);

		pbFiat = (ProgressBar) v.findViewById(R.id.prog_fiat);
		tvFiatTitle = (TextView) v.findViewById(R.id.tv_frag_header_fiat);
		tvFiatSub1 = (TextView) v.findViewById(R.id.tv_frag_sub_fiat_1);
		tvFiatSub2 = (TextView) v.findViewById(R.id.tv_frag_sub_fiat_2);
		if(isLaunching) {//first launch
			new GetCurrencyAsync().execute(tvSatSub2);
			new GetCurrencyAsync().execute(tvFiatSub2);
			isLaunching = false;
		}

		return v;
	}

	public static CurrencyFragment newInstance(int sectionNumber) {
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
		MenuItem itemRefresh = mMenu.findItem(R.id.menu_currency_refresh);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		View abprogress = inflater.inflate(R.layout.progress_wheel, null);
		itemRefresh.setActionView(abprogress);

		pbSat.setVisibility(View.VISIBLE);
		pbFiat.setVisibility(View.VISIBLE);
		tvSatSub1.setVisibility(View.GONE);
		tvSatSub2.setVisibility(View.GONE);
		tvFiatSub1.setVisibility(View.GONE);
		tvFiatSub2.setVisibility(View.GONE);
		new GetCurrencyAsync().execute(tvSatSub2);
		new GetCurrencyAsync().execute(tvFiatSub2);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!((MainActivity)getActivity()).getNavigationDrawerFragment().isDrawerOpen()) {
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

		private String urlGetDogeSat = "https://block" +
				".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=BTC";
		private String urlGetDogeLocal;
		TextView tv;

		@Override
		protected void onPreExecute() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String local = prefs.getString(PREF_LOCAL_CURRENCY, "USD");
			urlGetDogeLocal = "https://block" +
					".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=" +
					local;
		}

		@Override
		protected String doInBackground(TextView... params) {
			tv = params[0];
			String urlToGet;
			if (tv == tvSatSub2) {
				urlToGet = urlGetDogeSat;
			} else {
				urlToGet = urlGetDogeLocal;
			}

			try {
				String s = new String(getUrlBytes(urlToGet));
				//Log.i("HTTP", s);
				//we have webdata, now to convert to JSON and parse
				JSONObject object = new JSONObject(s);
				if (object.getString("status").equals("success")) {
					JSONObject data = object.getJSONObject("data");
					JSONArray priceArray = data.getJSONArray("prices");

					if (priceArray.length() == 0) {
						//no trades found
						return getResources().getString(R.string.no_trades_found);
					} else {
						s = "";
						for (int i = 0; i < priceArray.length(); i++) {
							JSONObject o = priceArray.getJSONObject(i);
							double price;
							if (tv == tvSatSub2) {
								price = o.getDouble("price") * 100000000;
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
					//api fail
					return getResources().getString(R.string.api_fail);
				}

			} catch (ConnectException e) {
				return getResources().getString(R.string.network_fail);
			} catch (UnknownHostException e){
				return getResources().getString(R.string.no_network);
			}
			catch (Exception e) {
				e.printStackTrace();
				return getResources().getString(R.string.error);
			}

		}


		@Override
		protected void onPostExecute(final String s) {
			if(isAdded()) {
				if (s != null) {
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
				if(mMenu != null) {
					MenuItem itemRefresh = mMenu.findItem(R.id.menu_currency_refresh);
					if(itemRefresh!=null) itemRefresh.setActionView(null);
				}
			}
		}

		byte[] getUrlBytes(String urlSpec) throws IOException {
			URL url = new URL(urlSpec);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStream in = connection.getInputStream();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e("Fetchr", connection.getResponseMessage() + " : " + connection
							.getResponseCode());
					return null;
				}

				int bytesRead = 0;
				byte[] buffer = new byte[1024];
				while ((bytesRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
				return out.toByteArray();
			} finally {
				connection.disconnect();
			}
		}
	}

}
