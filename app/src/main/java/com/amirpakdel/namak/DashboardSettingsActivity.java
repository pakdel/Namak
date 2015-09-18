package com.amirpakdel.namak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class DashboardSettingsActivity extends NamakSettingsActivity {
    public static final String DASHBOARD_ID = "dashboard_id";
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
            bindPreferenceSummaryToValue(url);
            screen.addPreference(url);

            // TODO
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
                                    // TODO Clear other configurations as well
                                    edit.apply();
//                                    edit.commit();
                                    // TODO Do we need to signal changes?
//                                    recreateSaltMasterList();
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
