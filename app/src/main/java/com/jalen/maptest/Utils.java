package com.jalen.maptest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.awareness.snapshot.internal.Snapshot;

/**
 * Created by zj on 22/08/2016.
 */
public class Utils {

    static void showSnackbar(View view, int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_LONG)
                .show();
    }

    static void showOkSnackbar(View view, int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();
    }
}
