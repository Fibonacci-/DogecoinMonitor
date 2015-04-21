package com.helwigdev.a.dogecoinutilities;

import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by Tyler on 4/1/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class GraphDialog extends DialogFragment {


	public static GraphDialog newInstance(int title) {
		GraphDialog frag = new GraphDialog();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);

		return frag;
	}



//
//
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//		//setup view for dialog
//
//		int title = getArguments().getInt("title");
//		graph = new GraphView(getActivity());
//
//		for(LineGraphSeries s : seriesArray){
//			graph.addSeries(s);
//		}
//		//build it
//		return new AlertDialog.Builder(getActivity())
//				.setIcon(android.R.drawable.ic_dialog_info)
//				.setTitle(title)
//				.setView(graph)
//				.setPositiveButton(R.string.alert_dialog_ok,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int whichButton) {
//								dismiss();
//							}
//						}
//				)
//
//				.create();
//	}
}
