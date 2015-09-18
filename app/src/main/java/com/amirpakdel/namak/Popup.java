package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Popup {
    private static final String helpPageURL = NamakApplication.getAppContext().getString(R.string.helpPageURL);
    private static int veryLongDuration = 8000; // Snackbar.LENGTH_LONG
    public static void message(@NonNull CharSequence text) {
        Toast.makeText(NamakApplication.getAppContext(), text, Toast.LENGTH_SHORT)
                .show();
    }
    public static void error(@NonNull final Activity parent, @NonNull CharSequence text, final int code, @Nullable Throwable error) {
        if (error != null) {
            Log.e(String.format("Error Code %03d", code), error.toString(), error);
        }
        //noinspection ResourceType
        Snackbar.make(parent.getWindow().getDecorView().findViewById(android.R.id.content), text, veryLongDuration)
                .setAction("Details", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(String.format(helpPageURL, code)));
                                parent.startActivity(browserIntent);
                            }
                        }
                )
                .setActionTextColor(Color.GRAY)
                .show();
    }
}
