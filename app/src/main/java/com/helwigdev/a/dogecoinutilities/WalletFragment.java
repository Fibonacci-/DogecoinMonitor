package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
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
public class WalletFragment extends Fragment implements WalletListener {
	private static final String TAG = "WalletFragment";
	private static final String ARG_SECTION_NUMBER = "section_number";
	FragmentSingleton mFragmentSingleton;

	TableLayout mTotalDataTable;

	ArrayList<String> mWalletList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);

		mFragmentSingleton = FragmentSingleton.get(getActivity());
		mTotalDataTable = (TableLayout) v.findViewById(R.id.tl_wallet_total_data);

		TextView tvText = (TextView) v.findViewById(R.id.tv_wallet_total_fiat_header);

		mWalletList = mFragmentSingleton.getAddressList();
		String toAdd = "";

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
		//TODO
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
						Log.e("TAG", "Error getting wallet balance: " + connection.getResponseMessage() + " : " + connection.getResponseCode());
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
