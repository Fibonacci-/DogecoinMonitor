package com.helwigdev.a.dogecoinutilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

/**
 * Created by Tyler on 4/1/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class GraphDialog extends DialogFragment{

	public ArrayList<LineGraphSeries<DataPoint>> seriesArray = new ArrayList<>();

	public static FirstAlertDialog newInstance(int title) {
		FirstAlertDialog frag = new FirstAlertDialog();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//setup view for dialog
		int title = getArguments().getInt("title");

		GraphView graph = (GraphView) getActivity().findViewById(R.id.graph);
		for(LineGraphSeries s : seriesArray){
			graph.addSeries(s);
		}
		//build it
		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(title)
				.setView(graph)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dismiss();
							}
						}
				)

				.create();
	}
}
