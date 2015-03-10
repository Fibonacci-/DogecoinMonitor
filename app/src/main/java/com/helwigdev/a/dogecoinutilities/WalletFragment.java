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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Tyler on 2/19/2015.
 * All code herein copyright Helwig Development 2/19/2015
 */
public class WalletFragment extends Fragment implements WalletListener, BtcFiatListener {
	private static final String TAG = "WalletFragment";
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String BLOCK_IO_KEY = "6411-6b24-8a06-218e";
	FragmentSingleton mFragmentSingleton;
	static Activity mActivity;

	TableLayout mTotalDataTable;
	TextView tvTotalDogeHeader;
	TextView tvTotalFiatHeader;
	TextView tvTotalBtcHeader;

	ArrayList<String> mWalletList;
	ArrayList<String[]> mAmountList;
	int mListLength = Integer.MAX_VALUE;
	int mTotalAddressGet = 0;

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
		mAmountList = new ArrayList<>();
		mListLength = mWalletList.size();
		mTotalAddressGet = 0;
		mTotalDataTable = (TableLayout) v.findViewById(R.id.tl_wallet_total_data);

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

		mTotalAddressGet++;
		mAmountList.add(new String[]{address, balance});
		if (mTotalAddressGet == mListLength) {
			//trigger BTC and fiat
		}
	}

	@Override
	public void onGetBtcFiatValues(String address, String btcValue, String fiatValue) {

	}

	public static class GetBtcFiat extends AsyncTask<ArrayList<String[]>, Void, ArrayList<String[]>> {

		public GetBtcFiat(BtcFiatListener listener, Context c) {
			mListener = listener;
			mContext = c;
		}

		BtcFiatListener mListener;
		Context mContext;

		@SafeVarargs
		@Override
		protected final ArrayList<String[]> doInBackground(ArrayList<String[]>... params) {
			ArrayList<String[]> toReturn = new ArrayList<>();
			for (ArrayList<String[]> a : params) {
				for (String[] s : a) {
					String address = s[0];
					String balance = s[1];

					if (address == null || balance == null) return null;

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
					String fiatBase = prefs.getString(CurrencyFragment.PREF_LOCAL_CURRENCY, "USD");
					String btcUrl = "https://block" +
							".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=BTC";
					String fiatUrl = "https://block" +
							".io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e&price_base=" + fiatBase;
					HttpURLConnection btcConnection = null;
					HttpURLConnection fiatConnection = null;
					try {
						btcConnection = (HttpURLConnection) new URL(btcUrl).openConnection();
						fiatConnection = (HttpURLConnection) new URL(fiatUrl).openConnection();


					}catch (Exception e){
							e.printStackTrace();
						return null;
					} finally {
						if(btcConnection != null){
							btcConnection.disconnect();
						}
						if(fiatConnection != null){
							fiatConnection.disconnect();
						}
					}

				}
			}
			return null;
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
