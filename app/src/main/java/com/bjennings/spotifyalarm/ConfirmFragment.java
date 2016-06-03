package com.bjennings.spotifyalarm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ConfirmFragment extends DialogFragment {
    private ConfirmEvents listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConfirmEvents) {
            listener = (ConfirmEvents) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ConfirmFragment.ConfirmEvents");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        String  message = args.getString("message"), positive = args.getString("positive"),
                negative = args.getString("negative");

        builder.setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onConfirm(args);
                        }
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (listener != null) {
                            listener.onCancel(args);
                        }
                    }
                });
        return builder.create();
    }

    public interface ConfirmEvents {
        void onConfirm(Bundle args);
        void onCancel(Bundle args);
    }
}