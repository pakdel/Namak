package com.amirpakdel.namak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import java.util.HashSet;
import java.util.Set;

import Namak.R;

public class DashboardSettingsActivity extends NamakSettingsActivity {
    public static final String DASHBOARD_ID = "dashboard_id";
    private static final String[] PREF_SUFFIXES = {/*"name", */"url"};

    private static String dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dashboard = getIntent().getExtras().getString(DASHBOARD_ID);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DashboardPreferenceFragment())
                .commit();
    }

    protected boolean canClose() {
        final SharedPreferences prefs = NamakApplication.getPref();
        for (String prefSuffix: PREF_SUFFIXES) {
            if( ! prefs.contains("dashboard_" + dashboard + "_" + prefSuffix) ) {
                Popup.error(this, getString(R.string.incomplete_settings), 500, null);
                return false;
            }
        }
        return true;
    }

    public static class DashboardPreferenceFragment extends NamakPreferenceFragment {
        protected void populateScreen(PreferenceScreen screen) {
            EditTextPreference name = new EditTextPreference(getActivity());
            name.setTitle(getString(R.string.pref_dashboard_name_title));
            name.setKey("dashboard_" + dashboard + "_name");
            bindPreferenceSummaryToValue(name);
            screen.addPreference(name);

            EditTextPreference url = new EditTextPreference(getActivity());
            url.setTitle(getString(R.string.pref_dashboard_url_title));
            url.setKey("dashboard_" + dashboard + "_url");
            bindUrlPreferenceSummaryToValue(url);
            screen.addPreference(url);

            // TODO Implement Timeout
            Preference timeout = new Preference(getActivity());
            timeout.setTitle("Timeout");
            timeout.setSummary("Not implemented yet");
            timeout.setEnabled(false);
            screen.addPreference(timeout);

            Preference remove = new Preference(getActivity());
            remove.setTitle(getString(R.string.pref_dashboard_remove_title));
            remove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder confirmRemove = new AlertDialog.Builder(preference.getContext());
                    confirmRemove.setMessage(getString(R.string.pref_dashboard_remove_message, prefs.getString("dashboard_" + dashboard + "_name", "This should never happen!")))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // https://code.google.com/p/android/issues/detail?id=27801
                                    Set<String> dashboards = new HashSet<>(prefs.getStringSet("dashboards", new HashSet<String>()));
                                    dashboards.remove(dashboard);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putStringSet("dashboards", dashboards);
                                    for (String prefSuffix : PREF_SUFFIXES) {
                                        edit.remove("dashboard_" + dashboard + "_" + prefSuffix);
                                    }
                                    edit.apply();
                                    getActivity().finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                    return true;
                }
            });
            remove.setIcon(android.R.drawable.ic_menu_delete);
            screen.addPreference(remove);
        }
    }
}
