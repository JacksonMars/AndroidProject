package com.example.androidproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

/**
 * Loading dialog.
 * Source: Custom Loading Alert Dialog - Android Studio Tutorial by Stevdza-San (YouTube)
 */
public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;

    LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog() {
        dialog.dismiss();
    }
}
