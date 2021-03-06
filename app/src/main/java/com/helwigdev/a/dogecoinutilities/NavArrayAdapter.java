package com.helwigdev.a.dogecoinutilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Tyler on 1/11/2015.
 * Copyright 2015 by Helwig Development
 */
public class NavArrayAdapter extends ArrayAdapter<String> {
	public NavArrayAdapter(Context context, int resource, int textViewResourceId,
						   String[] objects) {
		super(context, resource, textViewResourceId, objects);
		this.c = context;
		this.titleArray = objects;
	}

	Context c;
	String[] titleArray;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		Holder holder = null;

		if(row == null) {
			row = LayoutInflater.from(c).inflate(R.layout.fragment_navigation_drawer_list_item, parent, false);
			holder = new Holder();
			holder.mTitle = (TextView)row.findViewById(android.R.id.text1);

			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}
		holder.mTitle.setText(titleArray[position]);
		//implement nav drawer specific view things here

		return row;


	}
	public static class Holder {
		TextView mTitle;
	}
}