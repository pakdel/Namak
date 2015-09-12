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
    private Snackbar snackbar;
    public static void message(@NonNull CharSequence text) {
        Toast.makeText(NamakApplication.getAppContext(), text, Toast.LENGTH_SHORT)
                .show();
    }
    public static void error(@NonNull final Activity parent, @NonNull CharSequence text, final int code, @Nullable Throwable error) {
        if (error != null) {
            Log.e(String.format("Error Code %03d", code), error.toString(), error);
        }
        // FIXME translate "Details"
        // FIXME Put the URL in values
        Snackbar.make(parent.getWindow().getDecorView().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("Details", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(String.format("http://www.amirpakdel.com/2015/09/10/namak/#%03d", code)));
                                parent.startActivity(browserIntent);
                            }
                        }
                )
                .setActionTextColor(Color.GRAY)
                .show();
    }
}
