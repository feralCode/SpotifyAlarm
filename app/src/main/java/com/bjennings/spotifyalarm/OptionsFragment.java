package com.bjennings.spotifyalarm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class OptionsFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        String[] opts = {"Edit", "Delete"};
        builder.setItems(opts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 1:
                                ConfirmFragment confirm = new ConfirmFragment();
                                Bundle sendArgs = new Bundle();
                                sendArgs.putString("message", args.getString("message"));
                                sendArgs.putString("positive", args.getString("positive"));
                                sendArgs.putString("negative", args.getString("negative"));
                                sendArgs.putString("id", args.getString("id"));
                                confirm.setArguments(sendArgs);
                                confirm.show(getFragmentManager(), "confirm-" + args.getString("id"));
                                break;
                        }
                    }
                });
        return builder.create();
    }
}