package com.helwigdev.a.dogecoinutilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Tyler on 3/24/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class FirstAlertDialog extends DialogFragment {

	public static FirstAlertDialog newInstance(int title) {
		FirstAlertDialog frag = new FirstAlertDialog();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int title = getArguments().getInt("title");

		TextView tv = new TextView(getActivity());
		tv.setText(getResources().getString(R.string.welcome_text));
		int padding_in_dp = 6;  // 6 dps
		final float scale = getResources().getDisplayMetrics().density;
		int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

		tv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(title)
				.setView(tv)
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