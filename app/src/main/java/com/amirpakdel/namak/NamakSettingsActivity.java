package com.amirpakdel.namak;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.net.URL;

abstract public class NamakSettingsActivity extends AppCompatActivity {
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if(value.toString().isEmpty()) {
                Popup.message(NamakApplication.getAppContext().getString(R.string.pref_empty_value, preference.getTitle()));
                return false;
            }
            preference.setSummary(value.toString());
            return true;
        }
    };
    protected static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), NamakApplication.getAppContext().getString(R.string.pref_default_value))
        );
    }

    private static final Preference.OnPreferenceChangeListener sBindPasswordPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (value.toString().isEmpty()) {
                preference.setSummary("Empty!");
            } else {
                preference.setSummary("****************");
            }
            return true;
        }
    };
    protected static void bindPasswordPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPasswordPreferenceSummaryToValueListener);
        sBindPasswordPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "")
        );
    }

    private static final Preference.OnPreferenceChangeListener sBindUrlPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            try {
                preference.setSummary(new URL(value.toString()).toString());
                return true;
            } catch (MalformedURLException error) {
                Popup.message(error.getMessage());
                return false;
            }
        }
    };
    protected static void bindUrlPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindUrlPreferenceSummaryToValueListener);
        sBindUrlPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), NamakApplication.getAppContext().getString(R.string.pref_default_url))
        );
    }

    protected abstract boolean canClose();

    @Override
    public void onBackPressed() {
        if (canClose()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (canClose()) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    abstract protected static class NamakPreferenceFragment extends PreferenceFragment {
        protected SharedPreferences prefs;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            prefs = NamakApplication.getPref();
            final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            populateScreen(screen);
            setPreferenceScreen(screen);
        }
        abstract protected void populateScreen(PreferenceScreen screen);
    }

}
