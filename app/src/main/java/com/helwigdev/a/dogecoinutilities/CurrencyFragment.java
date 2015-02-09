package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

/**
 * Created by Tyler on 1/8/2015.
 */

/**
 * Features:
 * Show satoshi values
 * 1Ð = 1Ð
 * Trading values
 * Show fiat values
 */
public class CurrencyFragment extends Fragment {
	private static final String ARG_SECTION_NUMBER = "section_number";
	private String satoshiValue;
	SharedPreferences sharedPref;

	ProgressBar progSat;
	TextView tvSub1;
	TextView tvSub2;

	ProgressBar progFiat;
	TextView tvFiatHeader;
	TextView tvFiat1;
	TextView tvFiat2;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(
				getArguments().getInt(ARG_SECTION_NUMBER));

	}

	public static CurrencyFragment newInstance(int sectionNumber) {
		CurrencyFragment fragment = new CurrencyFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_currency, container, false);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		progSat = (ProgressBar) getActivity().findViewById(R.id.prog_sat);
		tvSub1 = (TextView) getActivity().findViewById(R.id.tv_frag_sub_satoshi_1);
		tvSub2 = (TextView) getActivity().findViewById(R.id.tv_frag_sub_satoshi_2);

		progFiat = (ProgressBar) getActivity().findViewById(R.id.prog_fiat);
		tvFiatHeader = (TextView) getActivity().findViewById(R.id.tv_frag_header_fiat);
		tvFiat1 = (TextView) getActivity().findViewById(R.id.tv_frag_sub_fiat_1);
		tvFiat2 = (TextView) getActivity().findViewById(R.id.tv_frag_sub_fiat_2);

		tvSub1.setVisibility(View.INVISIBLE);
		tvSub2.setVisibility(View.INVISIBLE);
		tvFiat1.setVisibility(View.INVISIBLE);
		tvFiat2.setVisibility(View.INVISIBLE);

		startLoadCurrencyData();
	}

	private void startLoadCurrencyData() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				//TODO get a loading/refresh icon in the ActionBar
				String preferredCurrency = sharedPref.getString("currencyDelim", "USD");
				final String fPrefCurrency = preferredCurrency;
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tvFiatHeader.setText(fPrefCurrency);
					}
				});

				//begin satoshi network items
				ArrayList<JSONObject> dogePriceArray;
				try {
					dogePriceArray = Utilities.getDogePrice("BTC");
				} catch (JSONException | TimeoutException | InterruptedException |
						ExecutionException e) {
					e.printStackTrace();
					return;
				}
				//end of satoshi network items

				//start of satoshi GUI items
				String trading = "";
				try {
					for (JSONObject o : dogePriceArray) {
						Log.d("JSON", o.toString());
						double price = o.getDouble("price");
						double sat = price * 100000000;

						Date d = new Date(o.getLong("time"));
						String sDate = new SimpleDateFormat("HH:mm:ss").format(d);
						trading += format("%.0f", sat) + " on " + o.getString("exchange") + " at "
								+ sDate + Utilities.newLine;
					}
				} catch (Exception e) {
					e.printStackTrace();
					trading = "Network error";
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvSub2.setTextColor(getResources().getColor(R.color.red));
						}
					});

				}

				final String fTrading = trading.trim();
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progSat.setVisibility(View.GONE);
						tvSub1.setVisibility(View.VISIBLE);
						tvSub2.setVisibility(View.VISIBLE);
						tvSub1.setText(getString(R.string.trading_at));
						tvSub2.setText(fTrading);
					}
				});
				//end of satoshi GUI items

				//start of fiat network items


				ArrayList<JSONObject> fiatPriceArray = new ArrayList<>();
				try {
					fiatPriceArray = Utilities.getDogePrice(preferredCurrency);
				} catch (JSONException | InterruptedException | ExecutionException |
						TimeoutException e) {
					e.printStackTrace();
				}
				//end of fiat network items

				//start of fiat GUI items
				String worth = "";
				try {
					for (JSONObject o : fiatPriceArray) {
						Log.d("JSON", o.toString());
						double price = o.getDouble("price");
						double sat = price * 1000;

						Date d = new Date(o.getLong("time"));
						String sDate = new SimpleDateFormat("HH:mm:ss").format(d);
						worth += format("%.5f", sat) + " on " + o.getString("exchange") + " at " +
								sDate + Utilities.newLine;
					}
				} catch (Exception e) {
					e.printStackTrace();
					worth = "Network error";
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvSub2.setTextColor(getResources().getColor(R.color.red));
						}
					});

				}
				final String fWorth = worth;

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progFiat.setVisibility(View.GONE);
						tvFiat1.setVisibility(View.VISIBLE);
						tvFiat2.setVisibility(View.VISIBLE);
						tvFiat1.setText(getString(R.string.worth_1000));
						tvFiat2.setText(fWorth.trim());
					}
				});

			}
		});
		t.start();

	}

}
