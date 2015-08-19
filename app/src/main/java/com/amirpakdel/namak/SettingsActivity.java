package com.amirpakdel.namak;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            preference.setSummary(value.toString());
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "")
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new NamakPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class NamakPreferenceFragment extends PreferenceFragment {
    // Caution: The preference manager does not currently store a strong reference to the listener.
    // You must store a strong reference to the listener, or it will be susceptible to garbage collection.
    // We recommend you keep a reference to the listener in the instance data of an object that will exist as long as you need the listener.
    // @SuppressWarnings("FieldCanBeLocal")
    // private static SharedPreferences.OnSharedPreferenceChangeListener prefChanged;

        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("saltmasters") || (key.startsWith("saltmaster_") && key.endsWith("_name"))) {
                    recreateSaltMasterList();
                }
                if(key.equals("dashboards") || (key.startsWith("dashboard_") && key.endsWith("_name"))) {
                    recreateDashboardList();
                }
            }
        };
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
            recreateSaltMasterList();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }

        private SharedPreferences prefs;
        private PreferenceCategory saltMasterCategory;
        private PreferenceCategory dashboardCategory;

        public class ButtonPreference extends Preference {
            public ButtonPreference(Context context) {
                super(context);
            }
            @Override
            protected View onCreateView(ViewGroup parent) {
                View view = super.onCreateView(parent);
//                view.setIc
                return view;
            }
        }
        private void recreateSaltMasterList() {
            saltMasterCategory.removeAll();
            for (String saltmaster : prefs.getStringSet("saltmasters", new HashSet<String>())) {
                populateSaltMasterScreen(saltmaster);
            }
            Preference addSaltMaster = new Preference(getActivity());
            addSaltMaster.setTitle(getString(R.string.pref_master_add_title));
            addSaltMaster.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // Find the next ID
                    Set<String> saltmasters = prefs.getStringSet("saltmasters", new HashSet<String>());
                    int nextSaltmaster = 1;
                    while (saltmasters.contains(String.valueOf(nextSaltmaster))) {
                        nextSaltmaster += 1;
                    }
                    String saltmaster = String.valueOf(nextSaltmaster);
                    saltmasters.add(saltmaster);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putStringSet("saltmasters", saltmasters);
                    edit.putString("saltmaster_" + saltmaster + "_name", getString(R.string.pref_master_name_default, nextSaltmaster));
                    edit.apply();
//                    edit.commit();
                    recreateSaltMasterList();
                    return true;
                }
            });
            addSaltMaster.setIcon(android.R.drawable.ic_menu_add);
            saltMasterCategory.addPreference(addSaltMaster);
        }
        private void populateSaltMasterScreen(final String saltmaster) {
            final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            // TODO is this needed?
//                screen.setDefaultValue(Boolean.TRUE);
            screen.setTitle(prefs.getString("saltmaster_" + saltmaster + "_name", "This should never happen!"));

            EditTextPreference name = new EditTextPreference(getActivity());
            name.setTitle(getString(R.string.pref_master_name_title));
            name.setKey("saltmaster_" + saltmaster + "_name");
            bindPreferenceSummaryToValue(name);
            screen.addPreference(name);

            EditTextPreference url = new EditTextPreference(getActivity());
            url.setTitle(getString(R.string.pref_master_url_title));
            url.setKey("saltmaster_" + saltmaster + "_url");
            bindPreferenceSummaryToValue(url);
            screen.addPreference(url);

            EditTextPreference username = new EditTextPreference(getActivity());
            username.setTitle(getString(R.string.pref_master_username_title));
            username.setKey("saltmaster_" + saltmaster + "_username");
            bindPreferenceSummaryToValue(username);
            screen.addPreference(username);

            EditTextPreference password = new EditTextPreference(getActivity());
            password.setTitle(getString(R.string.pref_master_password_title));
            password.setKey("saltmaster_" + saltmaster + "_password");
            password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    preference.setSummary("****************");
                    return true;
                }
            });
            password.setSummary("****************");
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
                                    Set<String> saltmasters = prefs.getStringSet("saltmasters", new HashSet<String>());
                                    saltmasters.remove(saltmaster);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putStringSet("saltmasters", saltmasters);
                                    // TODO Clear other configuratios as well
                                    edit.apply();
//                                    edit.commit();
                                    recreateSaltMasterList();
                                    screen.getDialog().dismiss();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                    return true;
                }
            });
            remove.setIcon(android.R.drawable.ic_menu_delete);
            screen.addPreference(remove);

            saltMasterCategory.addPreference(screen);
        }


        private void recreateDashboardList() {
            dashboardCategory.removeAll();
            for (String dashboard : prefs.getStringSet("dashboards", new HashSet<String>())) {
                populateDashboardScreen(dashboard);
            }

            Preference addDashboard = new Preference(getActivity());
            addDashboard.setTitle(getString(R.string.pref_dashboard_add_title));
            addDashboard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // Find the next ID
                    Set<String> dashboards = prefs.getStringSet("dashboards", new HashSet<String>());
                    int nextDashboard = 1;
                    while (dashboards.contains(String.valueOf(nextDashboard))) {
                        nextDashboard += 1;
                    }

                    String dashboard = String.valueOf(nextDashboard);
                    dashboards.add(dashboard);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putStringSet("dashboards", dashboards);
                    edit.putString("dashboard_" + dashboard + "_name", getString(R.string.pref_dashboard_name_default, nextDashboard));
                    edit.apply();
//                    edit.commit();
                    recreateDashboardList();
                    return true;
                }
            });
            addDashboard.setIcon(android.R.drawable.ic_menu_add);
            dashboardCategory.addPreference(addDashboard);
        }
        private void populateDashboardScreen(final String dashboard) {
            final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            // TODO is this needed?
//                screen.setDefaultValue(Boolean.TRUE);
            screen.setTitle(prefs.getString("dashboard_" + dashboard + "_name", "This should never happen!"));

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
                                    Set<String> dashboards = prefs.getStringSet("dashboards", new HashSet<String>());
                                    dashboards.remove(dashboard);

                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putStringSet("dashboards", dashboards);
                                    // TODO Clear other configuratios as well
                                    edit.apply();
//                                    edit.commit();
                                    recreateDashboardList();
                                    screen.getDialog().dismiss();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                    return true;
                }
            });
            remove.setIcon(android.R.drawable.ic_menu_delete);
            screen.addPreference(remove);

            dashboardCategory.addPreference(screen);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            prefs = getPreferenceManager().getSharedPreferences();

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());

            // Category: Salt Master
            saltMasterCategory = new PreferenceCategory(getActivity());
            saltMasterCategory.setTitle(getString(R.string.pref_master_category_title));
            screen.addPreference(saltMasterCategory);
            recreateSaltMasterList();

            // Category: Dashboard
            dashboardCategory = new PreferenceCategory(getActivity());
            dashboardCategory.setTitle(getString(R.string.pref_dashboard_category_title));
            screen.addPreference(dashboardCategory);
            recreateDashboardList();
            // TODO Add a default dashboard

            // Category: Misc
            PreferenceCategory miscCategory = new PreferenceCategory(getActivity());
            miscCategory.setTitle(getString(R.string.pref_misc_category_title));
            screen.addPreference(miscCategory);

            TimeoutPreference timeout = new TimeoutPreference(getActivity(), null);
            timeout.setTitle(getString(R.string.pref_timeout_title));
            timeout.setKey("timeout");
            miscCategory.addPreference(timeout);

            CheckBoxPreference autoExecute = new CheckBoxPreference(getActivity());
            autoExecute.setTitle(getString(R.string.pref_auto_execute_title));
            autoExecute.setSummary(getString(R.string.pref_auto_execute_summary));
            autoExecute.setKey("auto_execute");
            miscCategory.addPreference(autoExecute);

            setPreferenceScreen(screen);
        }
    }
}
