package com.helwigdev.a.dogecoinutilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;

import java.util.ArrayList;

/**
 * Created by Tyler on 4/21/2015.
 * Copyright 2015 by Helwig Development
 */
public class LineGraphDialog extends DialogFragment {

	public void setItems(ArrayList<Line> lineArray, String title){
		mLines = lineArray;
		mTitle = title;
	}

	ArrayList<Line> mLines;
	String mTitle;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){

		LineGraph li = new LineGraph(getActivity());
		for(Line l : mLines){
			li.addLine(l);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mTitle)
				.setView(li);
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
