package com.helwigdev.a.dogecoinutilities;

/**
 * Created by Tyler on 3/18/2015.
 * All code herein copyright Helwig Development 3/18/2015
 */
public interface WalletSecondaryListener {
	//define callback
	//potentially could integrate this and WalletListener, but it would be a little messier than I'd prefer
	void onGetBFBalance(Double[] balances);
}
