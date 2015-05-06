package com.amirpakdel.namak;

import android.content.Context;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Inspired by http://robobunny.com/blog_files/android_seekbar_preference/SeekBarPreference.java
 * and http://android.hlidskialf.com/blog/code/android-seekbar-preference
 */
public class TimeoutPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    public static final int DEFAULT_TIMEOUT = 9;
    private static final String androidns = "http://schemas.android.com/apk/res/android";
    private final Context mContext;
    private final int mDefault;
    private final int mMax;
    private SeekBar mSeekBar;
    private TextView mValueText;
    private int mValue;
    private String mSummary;

    public TimeoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mMax = attrs.getAttributeIntValue(androidns, "max", 3600);
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", DEFAULT_TIMEOUT);
        setValue(mDefault);
    }

    @Override
    public CharSequence getSummary() {
        return mSummary;
    }

    private String getSummary(int value) {
        if (value > 120) {
            return String.format("%1$,.1f minutes", value / 60.0);
        } else {
            return String.valueOf(value).concat(" seconds");
        }
    }

    private void setValue(int value) {
        mValue = value;
        mSummary = getSummary(value);
        setSummary(mSummary);
        persistInt(value);
    }

    private void setSeekBar() {
        mSeekBar.setMax((int) Math.sqrt(mMax) - 1);
        mSeekBar.setProgress((int) Math.sqrt(mValue - 1));
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(32);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);
        setSeekBar();
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(mDefault) : (int) defaultValue);
    }

    public void onProgressChanged(SeekBar seek, int progress, boolean fromTouch) {
        mValueText.setText(getSummary((1 + progress) * (1 + progress)));
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
//        notifyChanged();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int progress = mSeekBar.getProgress();
            int newValue = (1 + progress) * (1 + progress);
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }
}
