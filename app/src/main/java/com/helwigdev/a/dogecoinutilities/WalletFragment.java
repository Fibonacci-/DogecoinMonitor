package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class WalletFragment extends Fragment implements WalletListener{
	private static final String TAG = "WalletFragment";
	private static final String ARG_SECTION_NUMBER = "section_number";
	FragmentSingleton mFragmentSingleton;

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

		TextView tvText = (TextView) v.findViewById(R.id.tv_wallet_total_fiat_header);

		ArrayList<String> walletList = mFragmentSingleton.getAddressList();
		String toAdd = "";

		for(String s : walletList){
			toAdd += s + Utilities.newLine;
		}

		tvText.setText(toAdd);

		for(String s : walletList){
			new GetWalletBalance().execute(s);
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
	}

	public static class GetWalletBalance extends AsyncTask<String, Void, String>{

		private String apiUrl = "https://dogechain.info/chain/Dogecoin/q/addressbalance/";
		private WalletListener mWalletListener;
		private String mAddress;

		public GetWalletBalance(WalletListener walletListener){
			mWalletListener = walletListener;
		}

		@Override
		protected String doInBackground(String... params) {
			for(String s : params){
				mAddress = s;
				String urlSpec = apiUrl + s;
				HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) new URL(urlSpec).openConnection();

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = connection.getInputStream();
					if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
						Log.e("TAG","Error getting wallet balance: " + connection.getResponseMessage() + " : " + connection.getResponseCode());
						return null;
					}

					int bytesRead = 0;
					byte[] buffer = new byte[1024];
					while((bytesRead = in.read(buffer)) > 0){
						out.write(buffer, 0, bytesRead);
					}
					out.close();
					Log.i(TAG, out.toString());
					return out.toString();

				} catch (Exception e){
					Log.e(TAG, "Error getting wallet balance: " + e.toString());
				} finally {
					if(connection != null){
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
