package com.example.mysmartchess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;

import com.example.mysmartchess.R;

class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;
    private boolean notSuccessful = true;

    LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }


    //LoadingDialog(Activity myActivity) {
    // activity = myActivity;
    // }

    void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            Log.d("ciro", "foi dismissed com sucesso");
            if (notSuccessful)
                activity.finish();
            // your code after dissmiss dialog
        });

    }


    void dismissDialog() {
        notSuccessful = false;
        dialog.dismiss();
    }

}
