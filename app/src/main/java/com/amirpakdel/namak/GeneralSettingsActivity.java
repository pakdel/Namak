package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.HashSet;
import java.util.Set;

public class GeneralSettingsActivity extends NamakSettingsActivity {
    protected static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    protected boolean canClose() {
        final SharedPreferences prefs = NamakApplication.getPref();
        if (prefs.getStringSet("saltmasters", new HashSet<String>()).size() < 1
                || prefs.getStringSet("dashboards", new HashSet<String>()).size() < 1) {
            Popup.error(this, getString(R.string.incomplete_settings), 500, null);
            return false;
        }
        return true;
    }

    public static class GeneralPreferenceFragment extends NamakPreferenceFragment {
        // Caution: The preference manager does not currently store a strong reference to the listener.
        // You must store a strong reference to the listener, or it will be susceptible to garbage collection.
        // We recommend you keep a reference to the listener in the instance data of an object that will exist as long as you need the listener.
        // @SuppressWarnings("FieldCanBeLocal")
        // private static SharedPreferences.OnSharedPreferenceChangeListener prefChanged;

        private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("saltmasters") || (key.startsWith("saltmaster_") && key.endsWith("_name"))) {
                    recreateSaltMasterList();
                }
                if (key.equals("dashboards") || (key.startsWith("dashboard_") && key.endsWith("_name"))) {
                    recreateDashboardList();
                }
            }
        };

        @Override
        public void onResume() {
            super.onResume();
//            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
            NamakApplication.getPref().registerOnSharedPreferenceChangeListener(listener);
            recreateSaltMasterList();
            recreateDashboardList();
        }

        @Override
        public void onPause() {
            super.onPause();
//            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
            NamakApplication.getPref().unregisterOnSharedPreferenceChangeListener(listener);
        }


        private PreferenceCategory saltMasterCategory;
        private PreferenceCategory dashboardCategory;


        private class SaltMasterButton extends Preference {
            private String saltmaster;

            public SaltMasterButton(Context context, String saltmasterId) {
                super(context);
                saltmaster = saltmasterId;
                setTitle(prefs.getString("saltmaster_" + saltmaster + "_name", "This should never happen!"));
            }

            @Override
            protected void onClick() {
                Intent intent = new Intent(activity, SaltMasterSettingsActivity.class);
                intent.putExtra(SaltMasterSettingsActivity.SALTMASTER_ID, saltmaster);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
//                super.onClick();
            }
        }

        private class DashboardButton extends Preference {
            private String dashboard;

            public DashboardButton(Context context, String dashboardId) {
                super(context);
                dashboard = dashboardId;
                setTitle(prefs.getString("dashboard_" + dashboard + "_name", "This should never happen!"));
            }

            @Override
            protected void onClick() {
                Intent intent = new Intent(activity, DashboardSettingsActivity.class);
                intent.putExtra(DashboardSettingsActivity.DASHBOARD_ID, dashboard);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
//                super.onClick();
            }
        }

        private void recreateSaltMasterList() {
            saltMasterCategory.removeAll();
            for (String saltmaster : prefs.getStringSet("saltmasters", new HashSet<String>())) {
                SaltMasterButton editSaltMaster = new SaltMasterButton(getActivity(), saltmaster);
                saltMasterCategory.addPreference(editSaltMaster);
            }
            Preference addSaltMaster = new Preference(getActivity());
            addSaltMaster.setTitle(getString(R.string.pref_master_add_title));
            addSaltMaster.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // Find the next ID
                    // https://code.google.com/p/android/issues/detail?id=27801
                    Set<String> saltmasters = new HashSet<>(prefs.getStringSet("saltmasters", new HashSet<String>()));
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
//                    recreateSaltMasterList();
                    SaltMasterButton editSaltMaster = new SaltMasterButton(getActivity(), saltmaster);
                    saltMasterCategory.addPreference(editSaltMaster);
                    editSaltMaster.onClick();
                    return true;
                }
            });
            addSaltMaster.setIcon(android.R.drawable.ic_menu_add);
            saltMasterCategory.addPreference(addSaltMaster);
        }

        private void recreateDashboardList() {
            dashboardCategory.removeAll();
            for (String dashboard : prefs.getStringSet("dashboards", new HashSet<String>())) {
                DashboardButton editDashboard = new DashboardButton(getActivity(), dashboard);
                dashboardCategory.addPreference(editDashboard);
            }

            Preference addDashboard = new Preference(getActivity());
            addDashboard.setTitle(getString(R.string.pref_dashboard_add_title));
            addDashboard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    // Find the next ID
                    // https://code.google.com/p/android/issues/detail?id=27801
                    Set<String> dashboards = new HashSet<>(prefs.getStringSet("dashboards", new HashSet<String>()));
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
//                    recreateDashboardList();
                    DashboardButton editDashboard = new DashboardButton(getActivity(), dashboard);
                    dashboardCategory.addPreference(editDashboard);
                    editDashboard.onClick();
                    return true;
                }
            });
            addDashboard.setIcon(android.R.drawable.ic_menu_add);
            dashboardCategory.addPreference(addDashboard);
        }

        protected void populateScreen(PreferenceScreen screen) {
            // Category: Salt Master
            saltMasterCategory = new PreferenceCategory(getActivity());
            saltMasterCategory.setTitle(getString(R.string.pref_master_category_title));
            screen.addPreference(saltMasterCategory);
            recreateSaltMasterList();

            // Category: DashboardButton
            dashboardCategory = new PreferenceCategory(getActivity());
            dashboardCategory.setTitle(getString(R.string.pref_dashboard_category_title));
            screen.addPreference(dashboardCategory);
            recreateDashboardList();

            // Category: Misc
            PreferenceCategory miscCategory = new PreferenceCategory(getActivity());
            miscCategory.setTitle(getString(R.string.pref_misc_category_title));
            screen.addPreference(miscCategory);

            TimeoutPreference timeout = new TimeoutPreference(getActivity(), null);
            timeout.setTitle(getString(R.string.pref_timeout_title));
            timeout.setSummary(getString(R.string.pref_timeout_summary));
            timeout.setKey("timeout");
            miscCategory.addPreference(timeout);

            CheckBoxPreference autoExecute = new CheckBoxPreference(getActivity());
            autoExecute.setTitle(getString(R.string.pref_auto_execute_title));
            autoExecute.setSummary(getString(R.string.pref_auto_execute_summary));
            autoExecute.setKey("auto_execute");
            miscCategory.addPreference(autoExecute);
        }
    }
}
