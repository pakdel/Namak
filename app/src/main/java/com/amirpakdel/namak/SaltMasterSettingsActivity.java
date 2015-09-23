package com.amirpakdel.namak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import java.util.HashSet;
import java.util.Set;

public class SaltMasterSettingsActivity extends NamakSettingsActivity {
    public static final String SALTMASTER_ID = "saltmaster_id";
    private static final String[] PREF_SUFFIXES = {/*"name", */"url", "username", "password", "eauth"};

    private static String saltmaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saltmaster = getIntent().getExtras().getString(SALTMASTER_ID);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SaltMasterPreferenceFragment())
                .commit();
    }

    protected boolean canClose() {
        final SharedPreferences prefs = NamakApplication.getPref();
        for (String prefSuffix: PREF_SUFFIXES) {
            if( ! prefs.contains("saltmaster_" + saltmaster + "_" + prefSuffix) ) {
                Popup.error(this, getString(R.string.incomplete_settings), 500, null);
                return false;
            }
        }
        return true;
    }

    public static class SaltMasterPreferenceFragment extends NamakPreferenceFragment {
        protected void populateScreen(PreferenceScreen screen) {
            EditTextPreference name = new EditTextPreference(getActivity());
            name.setTitle(getString(R.string.pref_master_name_title));
            name.setKey("saltmaster_" + saltmaster + "_name");
            bindPreferenceSummaryToValue(name);
            screen.addPreference(name);

            EditTextPreference url = new EditTextPreference(getActivity());
            url.setTitle(getString(R.string.pref_master_url_title));
            url.setKey("saltmaster_" + saltmaster + "_url");
            bindUrlPreferenceSummaryToValue(url);
            screen.addPreference(url);

            EditTextPreference username = new EditTextPreference(getActivity());
            username.setTitle(getString(R.string.pref_master_username_title));
            username.setKey("saltmaster_" + saltmaster + "_username");
            bindPreferenceSummaryToValue(username);
            screen.addPreference(username);

            EditTextPreference password = new EditTextPreference(getActivity());
            password.setTitle(getString(R.string.pref_master_password_title));
            password.setKey("saltmaster_" + saltmaster + "_password");
            bindPasswordPreferenceSummaryToValue(password);
            screen.addPreference(password);


            ListPreference eauth = new ListPreference(getActivity());
            eauth.setTitle(getString(R.string.pref_master_eauth_title));
            eauth.setEntries(R.array.pref_master_eauth_entries);
            eauth.setEntryValues(R.array.pref_master_eauth_entries);
            eauth.setKey("saltmaster_" + saltmaster + "_eauth");
            bindPreferenceSummaryToValue(eauth);
            screen.addPreference(eauth);

            Preference remove = new Preference(getActivity());
            remove.setTitle(getString(R.string.pref_master_remove_title));
            remove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder confirmRemove = new AlertDialog.Builder(preference.getContext());
                    confirmRemove.setMessage(getString(R.string.pref_master_remove_message, prefs.getString("saltmaster_" + saltmaster + "_name", "This should never happen!")))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // https://code.google.com/p/android/issues/detail?id=27801
                                    Set<String> saltmasters = new HashSet<>(prefs.getStringSet("saltmasters", new HashSet<String>()));
                                    saltmasters.remove(saltmaster);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putStringSet("saltmasters", saltmasters);
                                    for (String prefSuffix : PREF_SUFFIXES) {
                                        edit.remove("saltmaster_" + saltmaster + "_" + prefSuffix);
                                    }
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
