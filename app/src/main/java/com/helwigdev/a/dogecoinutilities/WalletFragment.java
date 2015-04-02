package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class WalletFragment extends Fragment implements WalletListener, WalletSecondaryListener {
	//declarations
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
		//redundant right now - may use this later, so might as well leave it in until publishing
		// day
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		//setup views etc
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);

		CardView cvTotalDoge = (CardView) v.findViewById(R.id.card_view_wallet_total_doge);
		CardView cvTotalFiat = (CardView) v.findViewById(R.id.card_view_wallet_total_fiat);
		CardView cvTotalBtc = (CardView) v.findViewById(R.id.card_view_wallet_total_btc);

		cvTotalDoge.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//setup graph view for total doge
				ArrayList<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();
				//get list of wallets
				ArrayList<String> walletList = mFragmentSingleton.getAddressList();
				for (String s : walletList) {
					//get list of amounts and times
					//add new series for each wallet
					ArrayList<DatabaseHelper.TimedWallet> tWalletList = mFragmentSingleton.queryAmounts(s);
					DataPoint[] pointArray = new DataPoint[tWalletList.size()];
					for (int i = 0; i < tWalletList.size(); i++) {
						//fill dat array
						pointArray[i] = new DataPoint(new Date(tWalletList.get(i).timestamp), tWalletList.get(i).amount);
					}
					if (pointArray.length > 0) {
						seriesList.add(new LineGraphSeries<DataPoint>(pointArray));
					}
				}

				GraphDialog graphDialog = GraphDialog.newInstance(R.string.hello);
				graphDialog.setSeriesArray(seriesList);
				graphDialog.show(getFragmentManager(), "graph");
			}
		});
		cvTotalFiat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//setup graph view for total fiat
				//may need progressbar as this could take some time
				String localCurrency = PreferenceManager.
						getDefaultSharedPreferences(getActivity()).getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
				//get list of wallets
				ArrayList<String> walletList = mFragmentSingleton.getAddressList();
				ArrayList<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();

				for(String s : walletList){
					//get list of wallets with associated times
					ArrayList<DatabaseHelper.TimedWallet> tWalletList = mFragmentSingleton.queryAmounts(s);
					DataPoint[] pointArray = new DataPoint[tWalletList.size()];
					for (int i = 0; i < tWalletList.size(); i++) {
						//iterate through wallet list and make a new line for each

						DatabaseHelper.TimedWallet wallet = tWalletList.get(i);
						double rate = mFragmentSingleton.getRateNearTime(localCurrency, wallet.timestamp);
						if(rate != -1){
							//get amount in fiat
							double amount = rate * wallet.amount;
							pointArray[i] = new DataPoint(new Date(wallet.timestamp), amount);
						}else {
							pointArray[i] = new DataPoint(new Date(wallet.timestamp), 0);
						}
					}
					if (pointArray.length > 0) {
						seriesList.add(new LineGraphSeries<>(pointArray));
					}
				}


				GraphDialog graphDialog = GraphDialog.newInstance(R.string.hello);
				graphDialog.setSeriesArray(seriesList);
				graphDialog.show(getFragmentManager(), "graph");

			}
		});
		cvTotalBtc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//setup graph view for btc
				//progressbar etc
				ArrayList<String> walletList = mFragmentSingleton.getAddressList();
				ArrayList<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();

				for(String s : walletList){
					//get list of wallets with associated times
					ArrayList<DatabaseHelper.TimedWallet> tWalletList = mFragmentSingleton.queryAmounts(s);
					DataPoint[] pointArray = new DataPoint[tWalletList.size()];
					for (int i = 0; i < tWalletList.size(); i++) {
						//iterate through wallet list and make a new line for each

						DatabaseHelper.TimedWallet wallet = tWalletList.get(i);
						double rate = mFragmentSingleton.getRateNearTime("BTC", wallet.timestamp);
						if(rate != -1){
							//get amount in fiat
							double amount = rate * wallet.amount;
							pointArray[i] = new DataPoint(new Date(wallet.timestamp), amount);
						} else {
							pointArray[i] = new DataPoint(new Date(wallet.timestamp), 0);
						}
					}
					if (pointArray.length > 0) {
						seriesList.add(new LineGraphSeries<>(pointArray));
					}
				}

				GraphDialog graphDialog = GraphDialog.newInstance(R.string.hello);
				graphDialog.setSeriesArray(seriesList);
				graphDialog.show(getFragmentManager(), "graph");

			}
		});

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
			//start network tasks
			new GetWalletBalance(this).execute(s);
		}

		return v;
	}


	public static WalletFragment newInstance(int sectionNumber) {
		//same as all the others - callback to update nav drawer position
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
		//called when asynctask for balances completes
		//updates UI with formatted info and starts the next task
		try {
			float fBalance = Float.parseFloat(balance);//if the balance string cannot be parsed the code will exit here
			mFragmentSingleton.addAmount(address, fBalance);
		} catch (Exception ignored) {
		}

		/* Create a new row to be added. */
		TableRow tr = new TableRow(mActivity);
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

		//table row separator
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

	public String getStringFromUrl(String url) {
		HttpURLConnection connection = null;
		String toReturn = null;
		try {
			//TODO check response
			//Thread.sleep(100);//block.io doesn't like too many requests per second - we have to
			// artificially restrict it
			//TODO change limit when DB lookup is working correctly
			//TODO got some redundant network lookup code. consolidate
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
		} catch (Exception e) {
			return null;
		}
		return toReturn;
	}

	@Override
	public void onGetBFBalance(Double[] balances) {//onGetBtcFiatBalance
		/* Create a new row to be added. */
		if (balances != null) {
			//similar to above UI update method - but this time, we're updating a table in two
			// CardViews instead of one, so it'll look a bit messier
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
			tvBtc.setText(balances[0] + "");//implicit conversion to string
			TextView tvFiat = new TextView(getActivity());
			tvFiat.setText(balances[1] + "");

			tvBtc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
			tvFiat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

			//TODO is it possible to use just v instead of v and vv?
			trBtc.addView(tvBtc);
			View v = new View(getActivity());
			v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
			v.setBackgroundColor(getResources().getColor(R.color.amber_700_color));

			trFiat.addView(tvFiat);
			View vv = new View(getActivity());
			vv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
			vv.setBackgroundColor(getResources().getColor(R.color.amber_700_color));

			TextView tvFiatHeader = (TextView) getActivity().findViewById(R.id
					.tv_wallet_total_fiat_header);

			TextView tvBtcHeader = (TextView) getActivity().findViewById(R.id
					.tv_wallet_total_btc_header);
			TextView tvBtcSub = (TextView) getActivity().findViewById(R.id
					.tv_wallet_total_btc_sub);
			TextView tvFiatSub = (TextView) getActivity().findViewById(R.id
					.tv_wallet_total_fiat_sub);

			String localCurrency = PreferenceManager.getDefaultSharedPreferences(getActivity())
					.getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
			tvFiatHeader.setText(String.format(getResources().getString(R.string
					.wallet_total_fiat), localCurrency));

			tvBtcHeader.setText(getResources().getString(R.string.wallet_total_btc));

			String btcNumFormat = String.format("%.8f", totalBtc);
			String fiatNumFormat = String.format("%.2f", totalFiat);

			tvBtcSub.setText(String.format(getResources().getString(R.string
					.wallet_total_btc_sub_format), btcNumFormat));
			tvFiatSub.setText(String.format(getResources().getString(R.string
					.wallet_total_fiat_sub_format), fiatNumFormat, localCurrency));

			mTotalBtcTable.addView(trBtc);
			mTotalBtcTable.addView(v);

			mTotalFiatTable.addView(trFiat);
			mTotalFiatTable.addView(vv);


		}

	}


	public class GetConversionAmounts extends AsyncTask<String, Void, Double[]> {
		//set API locations
		String btc = "https://block.io/api/v1/get_current_price/?api_key=" + BLOCK_IO_KEY +
				"&price_base=" + "BTC";
		String fiatBase = PreferenceManager.getDefaultSharedPreferences(mActivity)
				.getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
		String fiat = "https://block.io/api/v1/get_current_price/?api_key=" + BLOCK_IO_KEY +
				"&price_base=" + fiatBase;

		WalletSecondaryListener mWalletSecondaryListener;

		public GetConversionAmounts(WalletSecondaryListener listener) {
			mWalletSecondaryListener = listener;
		}

		protected Double[] doInBackground(String... params) {
			for (String s : params) {//this will only pay attention to the first value in params - it's just a nice way of catching if there are no values in params
				//TODO optimize this!
				//rate, timestamp
				double[] btcRecents = mFragmentSingleton.getHelper().getMostRecentRate("BTC");
				double[] fiatRecents = mFragmentSingleton.getHelper().getMostRecentRate(fiatBase);
				double time = new Date().getTime();

				double fiveMinutesInMillis = 300000;

				if (btcRecents[0] != -1 && fiatRecents[0] != -1) {//if we have both values
					if ((time - fiveMinutesInMillis) > btcRecents[1] && (time -
							fiveMinutesInMillis) > fiatRecents[1]) {//if our values are less than
						// 5 minutes old
						float dogeBalance = Float.parseFloat(s);
						Log.i("BTCValuesGet", "Taking a shortcut - found old values for " +
								"currencies");
						return new Double[]{dogeBalance * btcRecents[0],
								dogeBalance * fiatRecents[0]};
					} else {
						Log.i(TAG, "DB data old - updating from network");
					}
				} else {
					Log.i(TAG, "DB data bad - updating from network");
				}

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

					//init floats to -1 - will be interpreted as errors when entered into DB

					float avgBtc = -1;

					if (btcArray.length() > 0) {
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

					if (fiatArray.length() > 0) {
						float addedFiatValues = 0;
						for (int i = 0; i < fiatArray.length(); i++) {
							JSONObject o = fiatArray.getJSONObject(i);
							addedFiatValues += o.getDouble("price");
							Log.i(TAG, "NetgetFiat: " + o.getDouble("price"));
						}
						avgFiat = addedFiatValues / fiatArray.length();
					}

					double dogeBalance = Double.parseDouble(s);
					Log.i(TAG, "Dogebalance is " + dogeBalance);
					mFragmentSingleton.getHelper().insertRate("BTC", avgBtc);
					mFragmentSingleton.getHelper().insertRate(fiatBase, avgFiat);
					return new Double[]{dogeBalance * avgBtc, dogeBalance * avgFiat};

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Double[] doubles) {
			mWalletSecondaryListener.onGetBFBalance(doubles);//interface callback when we jump back to the UI thread
		}
	}

	public static class GetWalletBalance extends AsyncTask<String, Void, String> {
		//get and parse network data
		//TODO really need to reduce redundancy here, way too much copy/pasted code

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
