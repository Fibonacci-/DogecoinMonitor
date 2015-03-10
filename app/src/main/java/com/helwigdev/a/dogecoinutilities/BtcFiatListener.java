package com.helwigdev.a.dogecoinutilities;

/**
 * Created by Tyler on 3/10/2015.
 * Copyright 2015 by Tyler Helwig
 */
public interface BtcFiatListener {
	void onGetBtcFiatValues(String address, String btcValue, String fiatValue);
}
