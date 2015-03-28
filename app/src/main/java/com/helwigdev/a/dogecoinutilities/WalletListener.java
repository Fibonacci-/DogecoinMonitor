package com.helwigdev.a.dogecoinutilities;

/**
 * Created by Tyler on 2/25/2015.
 * All code herein copyright Helwig Development 2/25/2015
 */
public interface WalletListener {
	//define callback
	void onGetAddressBalance(String address, String balance);
}
