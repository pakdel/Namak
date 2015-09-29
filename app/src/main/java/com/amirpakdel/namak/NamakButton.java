package com.amirpakdel.namak;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

public class NamakButton extends Button {
    private static final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);
    public NamakButton(Context context) {
        super(context);
        setLayoutParams(layoutParams);
        setBackgroundResource(R.drawable.button_background);
    }

}
