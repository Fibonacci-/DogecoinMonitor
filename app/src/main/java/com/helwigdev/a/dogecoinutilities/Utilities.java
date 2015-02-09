package com.helwigdev.a.dogecoinutilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Utilities {

	static String poolApiFileName = "poolApi.txt";
	static String walletListFileName = "walletList.txt";
	public static String newLine = System.getProperty("line.separator");

	/**
	 * This method reads from a predetermined file and returns an {@link java.util.ArrayList} of
	 * pool API strings.
	 *
	 * @param c The {@link android.content.Context} of the calling {@link android.app.Activity}.
	 * @return An {@link java.util.ArrayList} of pool API strings to be parsed.
	 */
	public static ArrayList<String> getPoolApiList(Context c) {
		File f = new File(c.getFilesDir().getAbsolutePath().concat(poolApiFileName));
		ArrayList<String> toReturn = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				toReturn.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	/**
	 * This method will overwrite all saved pools with the {@link java.util.ArrayList} passed as a
	 * parameter.<br><br>
	 * <p/>
	 * Use with caution: the Utilities.addPoolApi is preferred to use of this method.
	 *
	 * @param c     The {@link android.content.Context} of the calling activity.
	 * @param pools The {@link java.util.ArrayList} to overwrite the previously saved pools with.
	 * @return Whether the replacement operation completed successfully.
	 */
	public static boolean writeAllPoolApiList(Context c, ArrayList<String> pools) {
		File f = new File(c.getFilesDir().getAbsolutePath().concat(poolApiFileName));
		try {
			FileWriter f0 = new FileWriter(f);

			for (String s : pools) {
				f0.write(s + newLine);
			}
			f0.close();
			return true;
		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Add a pool to the list of previously saved APIs.
	 *
	 * @param c       The {@link android.content.Context} of the calling activity.
	 * @param poolApi The API {@link java.lang.String} to add to the list of APIs.
	 * @return Whether the operation completed successfully or not.
	 */
	public static boolean addPoolApi(Context c, String poolApi) throws IOException {
		if (!checkSavedPoolApi(c, poolApi)) {
			File f = new File(c.getFilesDir().getAbsolutePath().concat(poolApiFileName));

			BufferedWriter output = new BufferedWriter(new FileWriter(f, true));
			output.append(poolApi).append(newLine);
			output.close();
			Toast.makeText(c, "Added pool!", Toast.LENGTH_LONG).show();
			return true;

		}
		Toast.makeText(c, "Pool API key has already been added!", Toast.LENGTH_LONG).show();
		return false;
	}

	/**
	 * Returns true if the list of saved pools already contains the given {@link java.lang.String}.
	 *
	 * @param c       The {@link android.content.Context} of the calling activity.
	 * @param poolApi The pool API {@link java.lang.String} to be tested.
	 * @return Returns true if the pool has already been saved.
	 */
	public static boolean checkSavedPoolApi(Context c, String poolApi) {
		ArrayList<String> list = getPoolApiList(c);
		for (String s : list) {
			if (poolApi.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fetches an {@link java.util.ArrayList} of addresses saved in a predetermined file.
	 *
	 * @param c The {@link android.content.Context} of the calling activity.
	 * @return An {@link java.util.ArrayList} of addresses.
	 * @throws IOException
	 */
	public static ArrayList<String> getWalletAddressList(Context c) throws IOException {
		File f = new File(c.getFilesDir().getAbsolutePath().concat(walletListFileName));
		ArrayList<String> toReturn = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line;

		while ((line = reader.readLine()) != null) {
			toReturn.add(line);
		}
		reader.close();
		return toReturn;
	}


	/**
	 * This method adds an address string to the pre-existing address list.
	 * <p/>
	 * <br><br>Note that the address should be checked for validity before adding it to the list
	 * using this
	 * method, as the method does not do any sanity checks on the address.
	 *
	 * @param c         The {@link android.content.Context} of the calling {@link android.app
	 *                  .Activity}.
	 * @param address   The address to be saved.
	 * @param showToast Set to true in order to show a toast confirming that the address has been
	 *                  added successfully.
	 * @return Returns true if the address has been added successfully.
	 */
	public static boolean addWalletAddress(Context c, String address,
										   Boolean showToast) throws IOException {
		if (!checkSavedWalletAddress(c, address)) {

			File f = new File(c.getFilesDir().getAbsolutePath().concat(walletListFileName));
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(f, true));
				output.append(address).append(newLine);
				output.close();
				if (showToast) {
					Toast.makeText(c, "Added wallet " + address, Toast.LENGTH_LONG).show();
				}
				return true;
			} catch (IOException e) {

				e.printStackTrace();
				return false;
			}
		}
		if (showToast) {
			Toast.makeText(c, "Wallet address has already been added!", Toast.LENGTH_LONG).show();
		}
		return false;
	}

	/**
	 * A simple method to check if a wallet address has been previously saved.
	 *
	 * @param c       The {@link android.content.Context} of the calling activity.
	 * @param address The address {@link java.lang.String} to check.
	 * @return True if the address has been already saved in the list, false otherwise.
	 * @throws IOException If the file reading fails for any reason
	 */
	public static boolean checkSavedWalletAddress(Context c, String address) throws IOException {
		ArrayList<String> list = getWalletAddressList(c);
		for (String s : list) {
			if (address.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method will overwrite all saved wallets with the {@link java.util.ArrayList} passed
	 * as a
	 * parameter.<br><br>
	 * <p/>
	 * Use with caution: the Utilities.addWalletAddress is preferred to use of this method.
	 *
	 * @param c       The {@link android.content.Context} of the calling activity.
	 * @param wallets The {@link java.util.ArrayList} that will overwrite the saved wallet list.
	 * @return Whether the operation completed successfully - if false,
	 * check the event log for details.
	 */
	public static boolean writeAllWalletList(Context c, ArrayList<String> wallets) {
		File f = new File(c.getFilesDir().getAbsolutePath().concat(walletListFileName));
		try {
			FileWriter f0 = new FileWriter(f);

			for (String s : wallets) {
				f0.write(s + newLine);
			}
			f0.close();
			return true;
		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}

	}

	public static final int QR_TYPE_UNKNOWN = 0;
	public static final int QR_TYPE_WALLET_ADDRESS = 1;
	public static final int QR_TYPE_POOL_API_KEY = 2;
	public static final int QR_TYPE_JSON_ADDRESSES = 3;
	public static final int QR_TYPE_CLIENT_WALLET_URI = 4;

	/**
	 * This method is a simplistic way of determining the type of a scanned code.
	 * <br>
	 * <br>
	 * Note that it does not do any heavy verification of type, but merely scans the first
	 * character and takes a rough guess.
	 *
	 * @param codeResult The {@link java.lang.String} to check.
	 * @return The type of code. Types can be found in the Utilities class.
	 */
	@Deprecated
	public static int checkQRCodeType(String codeResult) {
		// returns 0 if not recognized,
		// returns 1 if wallet address,
		// returns 2 if pool API key
		//
		if (codeResult.substring(0, 1).equals("|")) {
			return QR_TYPE_POOL_API_KEY;
		} else if (codeResult.substring(0, 9).equals("dogecoin:")) {
			Log.d("Utilities", "Detected client URI");
			return QR_TYPE_CLIENT_WALLET_URI;
		} else if (codeResult.substring(0, 1).equals("D")) {
			return QR_TYPE_WALLET_ADDRESS;
		} else if (codeResult.substring(0, 1).equals("[")) {
			return QR_TYPE_JSON_ADDRESSES;
		} else {
			return QR_TYPE_UNKNOWN;
		}
	}

	/**
	 * Gets a JSON object from a given URL
	 *
	 * @param url The {@link java.lang.String} URL to get (the URL must return a JSON-parseable
	 *            response)
	 * @return The JSON object
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	protected static JSONObject getJsonObject(String url) throws InterruptedException,
			ExecutionException, TimeoutException {

		JsonGet j = new JsonGet(url, JsonGet.JSON_TYPE_OBJECT);
		FutureTask<Object> ft = new FutureTask<>(j);
		ExecutorService eservice = Executors.newFixedThreadPool(1);
		eservice.execute(ft);
		return (JSONObject) ft.get(60, TimeUnit.SECONDS);
	}

	/**
	 * Gets a JSON array from a given URL
	 *
	 * @param url The {@link java.lang.String} URL to get (the URL must return a JSON-parseable
	 *            response)
	 * @return The JSON array
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	protected static JSONArray getJsonArray(String url) throws InterruptedException,
			ExecutionException, TimeoutException {

		JsonGet j = new JsonGet(url, JsonGet.JSON_TYPE_ARRAY);
		FutureTask<Object> ft = new FutureTask<>(j);
		ExecutorService eservice = Executors.newFixedThreadPool(1);
		eservice.execute(ft);
		return (JSONArray) ft.get(60, TimeUnit.SECONDS);
	}

	/**
	 * Takes a few parsed pool API strings<br><br>
	 * Returns ArrayList of all workers found in that pool
	 *
	 * @param poolUrl The {@link java.lang.String} URL to request
	 * @return An {@link java.util.ArrayList} of {@link com.helwigdev.a.dogecoinutilities
	 * .PoolWorker} objects
	 * @throws JSONException        Thrown if the returned data cannot be parsed as a JSON object
	 *                              or array
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static ArrayList<PoolWorker> getWorkerInfo(String poolUrl,
													  String poolAPIKey) throws JSONException,
			InterruptedException, ExecutionException, TimeoutException {
		String urlToRequest = poolUrl + "&action=getuserworkers&api_key=" + poolAPIKey;
		JSONObject o = getJsonObject(urlToRequest);
		ArrayList<PoolWorker> workerArray = new ArrayList<>();

		JSONArray a = o.getJSONObject("getuserworkers").getJSONArray("data");
		int length = a.length();
		for (int i = 0; i < length; i++) {
			o = a.getJSONObject(i);
			String name = o.getString("username");
			double hashrate = o.getDouble("hashrate");
			workerArray.add(new PoolWorker(name, hashrate));
		}

		return workerArray;
	}

	/**
	 * Gets a wallet balance from dogechain.info
	 *
	 * @param address The address to get the balance from.
	 * @return The balance of the wallet
	 * @throws InterruptedException Thrown if the network thread is closed before the data is
	 *                              fully loaded
	 * @throws ExecutionException
	 * @throws TimeoutException     Thrown if the network was too slow to respond to the request
	 *                              in time
	 */
	public static double getWalletBalance(String address) throws InterruptedException,
			ExecutionException, TimeoutException {
		Log.d("getWalletBalance", "Getting wallet balance from " + address);
		String url = "http://dogechain.info/chain/Dogecoin/q/addressbalance/" + address;
		WebDataGet wdg = new WebDataGet(url);
		FutureTask<String> ft = new FutureTask<>(wdg);
		ExecutorService eservice = Executors.newFixedThreadPool(1);
		eservice.execute(ft);
		double result;

		Log.d("getWalletBalance", "Waiting for server response...");
		String stringResult = ft.get(1, TimeUnit.MINUTES);
		Log.d("getWalletBalance", "Server response is " + stringResult);
		result = Double.parseDouble(stringResult);


		Log.d("getWalletBalance", "Got wallet balance");
		return result;
	}

	/**
	 * Gets the current pool hashrate of a given pool. Must supply API key
	 *
	 * @param poolUrl    The first portion of the URL given by the pool QR
	 * @param poolAPIKey The API key given by the pool QR
	 * @return The pool hashrate
	 * @throws JSONException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static double getPoolHashrate(String poolUrl,
										 String poolAPIKey) throws JSONException,
			InterruptedException, ExecutionException, TimeoutException {
		String urlToRequest = poolUrl + "&action=getpoolhashrate&api_key=" + poolAPIKey;
		JSONObject o = getJsonObject(urlToRequest);

		return o.getJSONObject("getpoolhashrate").getDouble("data");
	}

	/**
	 * Gets the current price of 1&ETH; in the given price base. Docs are at https://block
	 * .io/api/simple/curl
	 *
	 * @param price_base The currency with which to compare Doge against
	 * @return An {@link java.util.ArrayList} of {@link org.json.JSONObject}s containing details
	 * on the current price of Doge.
	 * @throws JSONException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static ArrayList<JSONObject> getDogePrice(String price_base) throws JSONException,
			InterruptedException, ExecutionException, TimeoutException {
		String url = "https://block.io/api/v1/get_current_price/?api_key=6411-6b24-8a06-218e" +
				"&price_base=" + price_base;
		Log.d("DOGE",url);
		JSONObject o = getJsonObject(url);
		ArrayList<JSONObject> results = new ArrayList<>();

		JSONArray array = o.getJSONObject("data").getJSONArray("prices");
		for (int i = 0; i < array.length(); i++) {
			results.add(array.getJSONObject(i));
		}

		return results;
	}


	public static double convertFromUSD(String targetCurrency) throws JSONException,
			InterruptedException, ExecutionException, TimeoutException {
		String url = "http://currency-api.appspot.com/api/USD/" + targetCurrency + "" +
				".json?key=f68092cafdb7838c1b6c362a75b7493f314aece6";
		//TODO find a better currency API that supports more locales
		// 1 USD is worth {rate} of target currency
		JSONObject o = getJsonObject(url);
		double result = -1;

		result = o.getDouble("rate");
		Log.d("conversion", "Exchange rate from USD to " + targetCurrency + " is " + result);

		return result;
	}

	/**
	 * A string of all data the app uses. Should be used to backup all data for a wipe.
	 * @param c	The {@link android.content.Context} of the calling activity.
	 * @return	A {@link java.lang.String} containing all non-private app data.
	 * @throws IOException
	 */
	public static String backupFormat(Context c) throws IOException{
		ArrayList<String> poolList = getPoolApiList(c);
		ArrayList<String> walletList = getWalletAddressList(c);
		int numPools = poolList.size();
		int numWallets = walletList.size();
		String toReturn = "";
		// write: number of pools, then all pool strings
		toReturn += numPools + "";
		toReturn += newLine;
		for (String s : poolList) {
			toReturn += s + newLine;
		}
		// write: number of wallets, then all wallets
		toReturn += numWallets + "";
		toReturn += newLine;
		for (String s : walletList) {
			toReturn += s + newLine;
		}

		return toReturn;
	}

	/**
	 * Used to restore the app to a previous state using the string generated from Utilities.backupFormat
	 * @param c			The {@link android.content.Context} of the calling activity.
	 * @param backup	The backup file.
	 * @return			True if the operation completed successfully
	 */
	public static boolean restoreBackup(Context c, File backup) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(backup));
			int numPools = Integer.parseInt(br.readLine());
			ArrayList<String> poolList = new ArrayList<String>();
			for (int i = 0; i < numPools; i++) {
				poolList.add(br.readLine());
			}
			int numWallets = Integer.parseInt(br.readLine());
			ArrayList<String> walletList = new ArrayList<String>();
			for (int i = 0; i < numWallets; i++) {
				walletList.add(br.readLine());
			}
			br.close();
			writeAllPoolApiList(c, poolList);
			writeAllWalletList(c, walletList);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
