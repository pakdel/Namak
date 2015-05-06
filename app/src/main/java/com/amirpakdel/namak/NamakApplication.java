package com.amirpakdel.namak;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class NamakApplication extends android.app.Application
        implements SaltMaster.DashboardListener {
    private static Context context;
    private static SharedPreferences pref;
    // Caution: The preference manager does not currently store a strong reference to the listener.
    // You must store a strong reference to the listener, or it will be susceptible to garbage collection.
    // We recommend you keep a reference to the listener in the instance data of an object that will exist as long as you need the listener.
    @SuppressWarnings("FieldCanBeLocal")
    private static SharedPreferences.OnSharedPreferenceChangeListener prefChanged;

    private static boolean mAutoExecute;
    private static SaltMaster sm;
    private static RequestQueue queue;
    private static JSONObject dashboard;
    private static DashboardListAdapter dashboardListAdapter;

    public static Context getAppContext() {
        return context;
    }

    public static SaltMaster getSaltMaster() {
        return sm;
    }

    public static DashboardListAdapter getDashboardListAdapter() {
        return dashboardListAdapter;
    }

    public static void addToVolleyRequestQueue(Request req) {
        queue.add(req);
    }

    //    public static String getDashboardItemName(int position) {
//        return dashboardListAdapter.getItem(position);
//    }
//    public static JSONObject getDashboardItem(String dashboardItemName) throws JSONException {
//        return dashboard.getJSONObject(dashboardItemName);
//    }
    public static JSONObject getDashboardItem(int dashboardItemPosition) throws JSONException {
        return dashboard.getJSONObject(dashboardListAdapter.getItem(dashboardItemPosition));
    }

    public static boolean getAutoExecute() {
        return mAutoExecute;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        // DEBUG
//      PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
//      PreferenceManager.setDefaultValues(this, R.xml.pref, true);
        PreferenceManager.setDefaultValues(NamakApplication.context, R.xml.pref, false);

        mAutoExecute = pref.getBoolean("auto_execute", false);

        // Volley
        queue = Volley.newRequestQueue(context);

        sm = new SaltMaster(
                pref.getString("master", getString(R.string.pref_default_master)),
                pref.getString("dashboard", getString(R.string.pref_default_dashboard)),
                pref.getString("eauth", getString(R.string.pref_default_eauth)),
                pref.getString("username", getString(R.string.pref_default_username)),
                pref.getString("password", getString(R.string.pref_default_password)),
                pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT),
                this
        );

        prefChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                assert sm != null;
                Log.d("prefChanged", "Updating " + s);
                switch (s) {
                    case "master":
                        sm.setBaseUrl(pref.getString("master", getString(R.string.pref_default_master)));
                        break;
                    case "dashboard":
                        sm.setDashboardUrl(pref.getString("dashboard", getString(R.string.pref_default_dashboard)));
                        break;
                    case "eauth":
                        sm.setEauth(pref.getString("eauth", getString(R.string.pref_default_eauth)));
                        break;
                    case "username":
                        sm.setUsername(pref.getString("username", getString(R.string.pref_default_username)));
                        break;
                    case "password":
                        sm.setPassword(pref.getString("password", getString(R.string.pref_default_password)));
                        break;
                    case "timeout":
                        sm.setTimeout(pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT));
                        break;
                    case "auto_execute":
                        mAutoExecute = pref.getBoolean("auto_execute", false);
                        break;
                    default:
                        Log.e("prefChanged", s + " is not a know preference: " + pref.getString(s, "Failed to get " + s));
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(prefChanged);

        dashboardListAdapter = new DashboardListAdapter(context);
    }

    @Override
    public void onDashboardLoadFinished(JSONObject newDashboard) {
        if (newDashboard == null) {
            return;
        }
        NamakApplication.dashboard = newDashboard;
        dashboardListAdapter.clear();
        Iterator<String> labels = NamakApplication.dashboard.keys();
        while (labels.hasNext()) {
            dashboardListAdapter.add(labels.next());
        }
    }

}
