package com.helwigdev.a.dogecoinutilities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tyler on 1/8/2015.
 */

/**
 * Features:
 * Add pool by API
 * *Find a better way of saving pool info
 * Add wallets by QR/Data matrix
 */
public class PoolFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static PoolFragment newInstance(int sectionNumber) {
        PoolFragment fragment = new PoolFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pool, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
