package com.helwigdev.a.dogecoinutilities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Tyler on 1/10/2015.
 * Copyright 2015 by Helwig Development
 */
public class SettingsFragment extends PreferenceFragment {
	//simple PreferenceFragment host
	private static final String ARG_SECTION_NUMBER = "section_number";
	private static final String BACKUP_FILE_NAME = "dogecoin_backup.txt";
	private static final String TAG = "SettingsFragment";
	public static final String DONATE_ADDRESS = "DBeTGY7wuEvL17MPddbGNX9FyFkhWGS1pQ";
	IInAppBillingService mService;
    static FragmentSingleton mFragmentSingleton;
	ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name,
									   IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set up billing
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        mFragmentSingleton = FragmentSingleton.get(getActivity());
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);
		//simple is as simple does
        Preference from_text = findPreference("add_text_wallet");
        from_text.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //open up a text entry dialog to add a new wallet
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                dialog.setView(input);

                dialog.setPositiveButton(getResources().getString(R.string.validate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = input.getText().toString();
                        try {
							VerifyAndAdd task = new VerifyAndAdd(Utilities.checkQRCodeType(address));
							task.execute(address);
						} catch (Exception e){
                        	Toast.makeText(getActivity(), "Input was not a valid wallet!", Toast.LENGTH_LONG).show();
						}
                    }
                });

                dialog.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.show();
                return true;
            }
        });


		Preference pref_backup = findPreference("backup");
		pref_backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//get list of wallets and format
				ArrayList<String> walletList = FragmentSingleton.get(getActivity()).getAddressList();
				String toWrite = Utilities.backupFormat(walletList);
				try {
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
						//if we can read and write to the sd card
						File output = new File(Environment.getExternalStorageDirectory(), BACKUP_FILE_NAME);

						FileOutputStream fos = new FileOutputStream(output);
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
						writer.write(toWrite);
						writer.flush();
						writer.close();
						Toast.makeText(getActivity(), "Backup written to " + output.getAbsolutePath(), Toast.LENGTH_SHORT).show();
						return true;
					} else {
						throw new Exception("Could not write to external storage: not writable");
					}
				} catch (Exception e) {
					Log.e(TAG, "Could not write to backup file: " + e);
					Toast.makeText(getActivity(), "Could not write backup file to external storage!" +
							"\nIs your phone plugged into a computer?", Toast.LENGTH_SHORT).show();
					return false;
				}

			}
		});

		Preference pref_restore = findPreference("restore");
		pref_restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					File input = new File(Environment.getExternalStorageDirectory(), BACKUP_FILE_NAME);
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
							Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
						//can read

						BufferedReader br = new BufferedReader(new FileReader(input));
						//pool things are legacy - I'm putting them in now (4/9/15) in case we have
						//to re-implement pools at a later date
						int numPools = Integer.parseInt(br.readLine());//will always be 0
						ArrayList<String> poolList = new ArrayList<>();
						for (int i = 0; i < numPools; i++) {
							poolList.add(br.readLine());
						}
						int numWallets = Integer.parseInt(br.readLine());
						ArrayList<String> walletList = new ArrayList<>();
						for (int i = 0; i < numWallets; i++) {
							walletList.add(br.readLine());
						}
						br.close();
						FragmentSingleton fs = FragmentSingleton.get(getActivity());
						for (String s : walletList) {
							//since this is from a backup, all wallets should already have been checked against the api
							//if someone wants to break the app, they can go for it, I'll give them the option
							fs.addWallet(s);
						}
						Toast.makeText(getActivity(), "All set! Restored " + walletList.size() + " wallets", Toast.LENGTH_SHORT).show();
						return true;
					} else {
						throw new Exception("Can't read from sdcard: external storage not readable");
					}
				} catch (Exception e) {
					Log.e(TAG, "Could not read backup file: " + e);
					Toast.makeText(getActivity(), "Could not restore backup!", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});


		Preference donate_ads = findPreference("donateBilling");
		Preference attributions = findPreference("attributions");


		boolean areAdsRemoved = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean(MainActivity.PREF_ADS_REMOVED, false);
		donate_ads.setEnabled(!areAdsRemoved);
		if (areAdsRemoved) {
			donate_ads.setSummary(getResources().getString(R.string.thanks));
		}
		donate_ads.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//get unique ID for user
				String serial = Build.SERIAL;
				if (mService != null) {
					try {
						Bundle buyIntentBundle = mService.getBuyIntent(3, getActivity().getPackageName(),
								MainActivity.SKU_REMOVE_ADS, "inapp", serial);
						if (buyIntentBundle.getInt("RESPONSE_CODE") == 0) {
							Log.i("Billing start", "Got billing intent OK");
							PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
							getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
									SettingsActivity.PURCHASE_ADS_REQUEST_CODE, new Intent(), 0, 0, 0);
						}
					} catch (RemoteException | IntentSender.SendIntentException e) {
						e.printStackTrace();
					}
					return true;
				} else {
					Toast.makeText(getActivity(), "Could not initialize billing service. :-(", Toast.LENGTH_SHORT).show();
					return false;
				}
			}

		});

		attributions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {

				final TextView message = new TextView(getActivity());
				final SpannableString s =
						new SpannableString(getResources().getText(R.string.attributions));
				Linkify.addLinks(s, Linkify.ALL);
				message.setText(s);
				message.setMovementMethod(LinkMovementMethod.getInstance());


				AlertDialog d = new AlertDialog.Builder(getActivity())
						.setTitle(getResources().getString(R.string.title_attributions))
								//can't use string resource for the following because when the system grabs the string
								//from the XML file it erases all HTML formatting, removing the links.
						.setMessage(Html.fromHtml("This app uses unmodified code from the following sources:<br>" +
								"<a href='https://github.com/zxing/zxing'>Zebra Crossing project</a> for QR code reading, updated through maven on each build<br>" +
								"<a href='https://github.com/Androguide/HoloGraphLibrary'>HoloGraphLibrary</a> for graphing engine, last updated 4/21/2015"))
						.setIcon(android.R.drawable.ic_menu_info_details)
						.show();
				((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				return true;
			}
		});
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			getActivity().unbindService(mServiceConn);
		}
	}


	public static SettingsFragment newInstance(int sectionNumber) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

    //This task verifies that addresses are valid and adds them to the DB
    public class VerifyAndAdd extends AsyncTask<String, Void, Void> {
        //constructor with type already defined
        public VerifyAndAdd(int type) {
            mType = type;
        }

        int mType;

        //extract address data from various types and check each address before adding
        @Override
        protected Void doInBackground(String... params) {

            switch (mType) {
                case Utilities.QR_TYPE_CLIENT_WALLET_URI:
                    Log.i(TAG, "Unrecognized Wallet URI");
                    for (String s : params) {
                        addWalletAddress(s.replace("dogecoin:", ""));
                    }
                    break;

                case Utilities.QR_TYPE_JSON_ADDRESSES:
                    Log.i(TAG, "Got type JSON addresses");
                    try {
                        //should be an array
                        JSONArray array = new JSONArray(params[0]);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            addWalletAddress(o.getString("address"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    break;

                case Utilities.QR_TYPE_WALLET_ADDRESS:
                    Log.i(TAG, "Got type Wallet Address");
                    for (String s : params) {
                        addWalletAddress(s);
                    }
                    break;

                case Utilities.QR_TYPE_POOL_API_KEY:
                    Log.i(TAG, "Got type Pool API key");
                    for (String s : params) {
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
         * <p/>
         * NETWORK ACTIVITY IS DONE IN THIS METHOD
         * <p/>
         * Verifies a given Dogecoin address and adds it to the list, if it is valid
         *
         * @param address The address to add
         */
        private void addWalletAddress(String address) {
            String url = "https://dogechain.info/chain/Dogecoin/q/checkaddress/" + address;

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Invalid address: response is " + connection.getResponseCode());
                    return;
                }

                mFragmentSingleton.addWallet(address);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * NOT FOR USE ON MAIN THREAD
         * <p/>
         * NETWORK ACTIVITY IS DONE IN THIS METHOD
         * <p/>
         * Verifies a given Dogecoin address and adds it to the list, if it is valid
         *
         * @param apiData The pool API MPOS string to add
         */
        private void addPool(String apiData) {

        }


    }
}
