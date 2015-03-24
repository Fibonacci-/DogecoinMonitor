package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class WalletFragment extends Fragment implements WalletListener, WalletSecondaryListener {
	private static final String TAG = "WalletFragment";
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String BLOCK_IO_KEY = "6411-6b24-8a06-218e";
	FragmentSingleton mFragmentSingleton;
	static Activity mActivity;

	TableLayout mTotalDataTable;
	TableLayout mTotalBtcTable;
	TableLayout mTotalFiatTable;
	TextView tvTotalDogeHeader;
	TextView tvTotalFiatHeader;
	TextView tvTotalBtcHeader;

	ArrayList<String> mWalletList;

	private double totalDoge = 0.0;
	private double totalFiat = 0.0;
	private double totalBtc = 0.0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);

		mActivity = getActivity();

		totalDoge = 0.0;
		totalFiat = 0.0;
		totalBtc = 0.0;

		mFragmentSingleton = FragmentSingleton.get(getActivity());
		mWalletList = mFragmentSingleton.getAddressList();
		mTotalDataTable = (TableLayout) v.findViewById(R.id.tl_wallet_total_data);
		mTotalBtcTable = (TableLayout) v.findViewById(R.id.tl_wallet_total_btc_data);
		mTotalFiatTable = (TableLayout) v.findViewById(R.id.tl_wallet_total_fiat_data);

		tvTotalFiatHeader = (TextView) v.findViewById(R.id.tv_wallet_total_fiat_header);
		tvTotalDogeHeader = (TextView) v.findViewById(R.id.tv_wallet_total_doge_header);
		tvTotalBtcHeader = (TextView) v.findViewById(R.id.tv_wallet_total_btc_header);

		tvTotalDogeHeader.setText(getResources().getText(R.string.totalDogePreformat));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String localCurrency = prefs.getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
		tvTotalFiatHeader.setText(String.format(getResources().getString(R.string
				.wallet_total_fiat), localCurrency));
		tvTotalBtcHeader.setText(getResources().getText(R.string.wallet_total_btc));

		for (String s : mWalletList) {
			new GetWalletBalance(this).execute(s);
		}

		return v;
	}


	public static WalletFragment newInstance(int sectionNumber) {
		WalletFragment fragment = new WalletFragment();
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

	@Override
	public void onGetAddressBalance(String address, String balance) {

		/* Create a new row to be added. */
		TableRow tr = new TableRow(getActivity());
		tr.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

		TextView tvAddress = new TextView(getActivity());
		tvAddress.setText(address);
		TextView tvBalance = new TextView(getActivity());
		tvBalance.setText(balance);

		tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		tvBalance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

		tvBalance.setGravity(Gravity.END);

		tr.addView(tvAddress);
		tr.addView(tvBalance);

		View v = new View(getActivity());
		v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
		v.setBackgroundColor(getResources().getColor(R.color.amber_700_color));

		mTotalDataTable.addView(tr);
		mTotalDataTable.addView(v);
		Log.i(TAG, "Added new table row");

		try {
			totalDoge += Double.valueOf(balance);
		} catch (Exception e) {
			e.printStackTrace();
		}

		tvTotalDogeHeader.setText(String.format(getResources().getString(R.string
				.wallet_total_doge_header_format), totalDoge));

		new GetConversionAmounts(this).execute(balance);

	}

	public String getStringFromUrl(String url){
		HttpURLConnection connection = null;
		String toReturn = null;
		try{
			Thread.sleep(100);
			//TODO check response
			connection = (HttpURLConnection) new URL(url).openConnection();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.e("TAG", "Error getting data from url: " + connection
						.getResponseMessage() + " : " + connection.getResponseCode());
				return null;
			}
			InputStream in = connection.getInputStream();
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			toReturn = out.toString();
		} catch (Exception e){
			return null;
		}
		return toReturn;
	}

	@Override
	public void onGetBFBalance(Float[] balances) {
		/* Create a new row to be added. */
		if(balances != null) {
			TableRow trBtc = new TableRow(getActivity());
			trBtc.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

			TableRow trFiat = new TableRow(getActivity());
			trFiat.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

			totalBtc += balances[0];
			totalFiat += balances[1];

			Log.i(TAG, "Got btc amount:" + balances[0] + " and fiat amount: " + balances[1]);

			TextView tvBtc = new TextView(getActivity());
			tvBtc.setText(balances[0] + "");
			TextView tvFiat = new TextView(getActivity());
			tvFiat.setText(balances[1] + "");

			tvBtc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
			tvFiat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

			trBtc.addView(tvBtc);
			View v = new View(getActivity());
			v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
			v.setBackgroundColor(getResources().getColor(R.color.amber_700_color));

			trFiat.addView(tvFiat);
			View vv = new View(getActivity());
			vv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
			vv.setBackgroundColor(getResources().getColor(R.color.amber_700_color));

			TextView tvFiatHeader = (TextView) getActivity().findViewById(R.id.tv_wallet_total_fiat_header);

			TextView tvBtcHeader = (TextView) getActivity().findViewById(R.id.tv_wallet_total_btc_header);
			TextView tvBtcSub = (TextView) getActivity().findViewById(R.id.tv_wallet_total_btc_sub);
			TextView tvFiatSub = (TextView) getActivity().findViewById(R.id.tv_wallet_total_fiat_sub);

			String localCurrency = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
			tvFiatHeader.setText(String.format(getResources().getString(R.string.wallet_total_fiat), localCurrency));

			tvBtcHeader.setText(getResources().getString(R.string.wallet_total_btc));

			String btcNumFormat = String.format("%.8f", totalBtc);
			String fiatNumFormat = String.format("%.2f", totalFiat);

			tvBtcSub.setText(String.format(getResources().getString(R.string.wallet_total_btc_sub_format), btcNumFormat));
			tvFiatSub.setText(String.format(getResources().getString(R.string.wallet_total_fiat_sub_format), fiatNumFormat, localCurrency));

			mTotalBtcTable.addView(trBtc);
			mTotalBtcTable.addView(v);

			mTotalFiatTable.addView(trFiat);
			mTotalFiatTable.addView(vv);
		}

	}


	public class GetConversionAmounts extends AsyncTask<String, Void, Float[]> {
		String btc = "https://block.io/api/v1/get_current_price/?api_key=" + BLOCK_IO_KEY +
				"&price_base=" + "BTC";
		String fiat = "https://block.io/api/v1/get_current_price/?api_key=" + BLOCK_IO_KEY +
				"&price_base=" + PreferenceManager.getDefaultSharedPreferences(mActivity)
				.getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");

		WalletSecondaryListener mWalletSecondaryListener;

		public GetConversionAmounts(WalletSecondaryListener listener){
			mWalletSecondaryListener = listener;
		}

		protected Float[] doInBackground(String... params) {
			for(String s : params){
				//TODO optimize this shit
				String btcRaw = getStringFromUrl(btc);
				String fiatRaw = getStringFromUrl(fiat);

				try {
					//step through json string
					JSONObject btcObject = new JSONObject(btcRaw);
					JSONObject fiatObject = new JSONObject(fiatRaw);

					JSONObject btcData = btcObject.getJSONObject("data");
					JSONObject fiatData = fiatObject.getJSONObject("data");

					JSONArray btcArray = btcData.getJSONArray("prices");
					JSONArray fiatArray = fiatData.getJSONArray("prices");

					float avgBtc = -1;

					if(btcArray.length() > 0) {
						//get average value for btc
						float addedBtcValues = 0;
						for (int i = 0; i < btcArray.length(); i++) {
							JSONObject o = btcArray.getJSONObject(i);
							addedBtcValues += o.getDouble("price");
							Log.i(TAG, "NetgetBTC: " + o.getDouble("price"));
						}
						avgBtc = addedBtcValues / btcArray.length();
					}

					float avgFiat = -1;

					if(fiatArray.length() > 0) {
						float addedFiatValues = 0;
						for (int i = 0; i < fiatArray.length(); i++) {
							JSONObject o = fiatArray.getJSONObject(i);
							addedFiatValues += o.getDouble("price");
							Log.i(TAG, "NetgetFiat: " + o.getDouble("price"));
						}
						avgFiat = addedFiatValues / fiatArray.length();
					}

					float dogeBalance = Float.parseFloat(s);
					Log.i(TAG, "Dogebalance is " + dogeBalance);
					return new Float[]{dogeBalance * avgBtc, dogeBalance * avgFiat};

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Float[] floats) {
			mWalletSecondaryListener.onGetBFBalance(floats);
		}
	}

	public static class GetWalletBalance extends AsyncTask<String, Void, String> {

		private String apiUrl = "https://dogechain.info/chain/Dogecoin/q/addressbalance/";
		private WalletListener mWalletListener;
		private String mAddress;

		public GetWalletBalance(WalletListener walletListener) {
			mWalletListener = walletListener;
		}

		@Override
		protected String doInBackground(String... params) {
			for (String s : params) {

				mAddress = s;
				String urlSpec = apiUrl + s;
				HttpURLConnection connection = null;
				try {
					Thread.sleep(100);
					connection = (HttpURLConnection) new URL(urlSpec).openConnection();

					ByteArrayOutputStream out = new ByteArrayOutputStream();

					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.e("TAG", "Error getting wallet balance: " + connection
								.getResponseMessage() + " : " + connection.getResponseCode());
						return null;
					}
					InputStream in = connection.getInputStream();
					int bytesRead = 0;
					byte[] buffer = new byte[1024];
					while ((bytesRead = in.read(buffer)) > 0) {
						out.write(buffer, 0, bytesRead);
					}
					out.close();
					Log.i(TAG, out.toString());
					return out.toString();

				} catch (Exception e) {
					Log.e(TAG, "Error getting wallet balance: ");
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			mWalletListener.onGetAddressBalance(mAddress, s);
		}
	}
}
