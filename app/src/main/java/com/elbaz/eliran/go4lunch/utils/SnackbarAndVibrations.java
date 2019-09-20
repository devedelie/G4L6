package com.elbaz.eliran.go4lunch.utils;

import android.content.Context;
import android.os.Vibrator;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by Eliran Elbaz on 20-Sep-19.
 */
public class SnackbarAndVibrations {
    public static void showSnakbarMessage(View rootView, String mMessage) {
        Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG)
                .show();
    }

    // Vibration method
    public static void Vibration (Context context){
        Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(50);
    }
}
