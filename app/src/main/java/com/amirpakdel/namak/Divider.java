package com.amirpakdel.namak;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

public class Divider extends View {

    public Divider(Context context) {
        super(context);
        final Resources r = getResources();
        final LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(
//                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics()),  // 1dp
                (int) r.getDisplayMetrics().density,  // 1dp
                LinearLayout.LayoutParams.MATCH_PARENT);
        dividerLayoutParams.setMargins(
                (int) r.getDimension(R.dimen.activity_horizontal_margin_small),
                (int) r.getDimension(R.dimen.activity_vertical_margin_small),
                (int) r.getDimension(R.dimen.activity_horizontal_margin_small),
                (int) r.getDimension(R.dimen.activity_vertical_margin_small));
        setLayoutParams(dividerLayoutParams);
//        setBackgroundResource(R.color.DarkBlue);
//        setBackgroundResource(R.color.LightBlue);
        setBackgroundResource(android.R.color.darker_gray);
    }
}
