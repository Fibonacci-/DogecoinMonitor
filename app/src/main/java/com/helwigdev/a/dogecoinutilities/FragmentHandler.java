package com.helwigdev.a.dogecoinutilities;

import android.util.Log;

/**
 * Created by Tyler on 1/8/2015.
 */
public class FragmentHandler {
    private static FragmentHandler instance = null;
    private static CurrencyFragment currencyFragment = null;
    private static PoolFragment poolFragment = null;

    protected FragmentHandler(){
        //exists to defeat instantiation
    }

    public static FragmentHandler getInstance() {
        if(instance == null) {
            instance = new FragmentHandler();
        }
        return instance;
    }

    public CurrencyFragment getCurrencyFragment(int position){
        if(currencyFragment == null){
            currencyFragment = CurrencyFragment.newInstance(position);
        }
        return currencyFragment;
    }

    public PoolFragment getPoolFragment(int position){
        if(poolFragment == null){
            poolFragment = PoolFragment.newInstance(position);
        }
        return poolFragment;
    }

}
