package com.amirpakdel.namak;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamakApplication extends android.app.Application {
    private static Context context;
    private static SharedPreferences pref;
    // Caution: The preference manager does not currently store a strong reference to the listener.
    // You must store a strong reference to the listener, or it will be susceptible to garbage collection.
    // We recommend you keep a reference to the listener in the instance data of an object that will exist as long as you need the listener.
    @SuppressWarnings("FieldCanBeLocal")
    private static SharedPreferences.OnSharedPreferenceChangeListener prefChanged;

    private static boolean mAutoExecute;
    private static String[] saltmasters;
    private static SaltMaster sm;
    private static RequestQueue queue;
    private static final ArrayList<DashboardListener> mDashboardListeners = new ArrayList<>(2);
    private static List<JSONObject> dashboard = new ArrayList<>(20);
    private static int dashboardTimeout = TimeoutPreference.DEFAULT_TIMEOUT;
    private static DashboardListAdapter dashboardListAdapter;

    public static Context getAppContext() {
        return context;
    }

    public static SaltMaster getSaltMaster() {
        return sm;
    }

    public static SharedPreferences getPref() {
        return pref;
    }

    private static void loadSaltmasters() {
        Set<String> saltmasterStringSet = pref.getStringSet("saltmasters", new HashSet<String>());
        saltmasters = new String[saltmasterStringSet.size()];
        saltmasters = saltmasterStringSet.toArray(saltmasters);
    }
    public static String[] getSaltmasterNames() {
        // FIXME reload only if changed
        loadSaltmasters();
        String[] ret = new String[saltmasters.length];
        for (int i=0; i<saltmasters.length; i++) {
            ret[i] = pref.getString("saltmaster_" + saltmasters[i] + "_name", "No Name ("+i+")" /*getString(R.string.pref_default_master)*/);
        }
        return ret;
    }
    public static String getSaltMasterId(int index) {
        return saltmasters[index];
    }
//    public static int getSaltMasterIndex(String id) {
//        return Arrays.binarySearch(saltmasters, id);
//    }
    public static int getSaltMasterIndex() {
        if (sm == null || sm.getId() == null || saltmasters == null) { return -1; }
        return Arrays.binarySearch(saltmasters, sm.getId());
    }

    public static DashboardListAdapter getDashboardListAdapter() {
        return dashboardListAdapter;
    }

    public static void addToVolleyRequestQueue(Request req) {
        queue.add(req);
    }

    public static JSONObject getDashboardItem(int dashboardItemPosition) throws JSONException {
        return dashboard.get(dashboardItemPosition);
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
//        PreferenceManager.setDefaultValues(NamakApplication.context, R.xml.pref, false);

        mAutoExecute = pref.getBoolean("auto_execute", false);

        // Volley
        queue = Volley.newRequestQueue(context);

        sm = new SaltMaster( pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT) );

        prefChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                assert sm != null;
                Log.d("prefChanged", "Updating " + key);
                // FIXME We should not reload all dashboards when just one is changed
                if (key.equals("dashboards") || (key.startsWith("dashboard_") && key.endsWith("_url"))) {
                    reloadDashboards();
                    return;
                }
                if (key.startsWith("saltmaster_"+sm.getId())) {
                    sm.login();
                    return;
                }

                switch (key) {
                    case "timeout":
                        sm.setTimeout(pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT));
                        break;
                    case "auto_execute":
                        mAutoExecute = pref.getBoolean("auto_execute", false);
                        break;
                    default:
                        Log.e("prefChanged", "Not grabbing changes of this preference: " + key);
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(prefChanged);

        dashboardListAdapter = new DashboardListAdapter(context);
        // dashboardListAdapter is used in reloadDashboards, but shouldn't
        reloadDashboards();
    }


    //    public void setAuthToken(String mAuthToken) { this.mAuthToken = mAuthToken; }
    public static void addDashboardListener(DashboardListener dashboardListener) {
        mDashboardListeners.add(dashboardListener);
        dashboardListener.onDashboardLoadFinished(/*mDashboard*/);
    }

    public interface DashboardListener {
        // If loading the mDashboard has not been successful, mDashboard will be null
        void onDashboardLoadFinished(/*JSONArray dashboard*/);
    }

    private static void triggerDashboardListeners() {
        for (DashboardListener dashboardListener : mDashboardListeners) {
            dashboardListener.onDashboardLoadFinished(/*mDashboard*/);
        }
    }

    public static void reloadDashboards() {
        NamakApplication.dashboard.clear();
        dashboardListAdapter.clear();
        // Need to run mSwipeRefreshLayout.setRefreshing(false);
        for (String dashboard : pref.getStringSet("dashboards", new HashSet<String>())) {
            loadDashboard(dashboard);
        }

    }

    private static void loadDashboard(String dashboard) {
        final String dashboardName = pref.getString("dashboard_" + dashboard + "_name", null);
        String dashboardUrl  = pref.getString("dashboard_" + dashboard + "_url", null);
        JsonArrayRequest dashboardRequest = new JsonArrayRequest(dashboardUrl,
                new Response.Listener<JSONArray>() {
                    //                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            onDashboardLoadFinished(response);
                            Log.d("SaltMaster: dashboard", response.toString(2).substring(0, 50));
                            Toast.makeText(NamakApplication.getAppContext(), "Loaded dashboard "+dashboardName, Toast.LENGTH_SHORT).show();
                        } catch (JSONException error) {
//                            mDashboard = null;
                            Toast.makeText(NamakApplication.getAppContext(), "Unexpected response when getting dashboard "+dashboardName, Toast.LENGTH_SHORT).show();
                            Log.e("SaltMaster: login", error.toString(), error);
                            Log.d("SaltMaster: dashboard", response.toString().substring(0, 50));
                        }
                        triggerDashboardListeners();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NamakApplication.getAppContext(), "Failed to load dashboard "+dashboardName, Toast.LENGTH_SHORT).show();
                        Log.e("SaltMaster: dash.Err", error.toString(), error);
                        triggerDashboardListeners();
                    }
                });
        // To set or not to set the timeout, that is the question
        dashboardRequest.setRetryPolicy(new DefaultRetryPolicy(dashboardTimeout * 1000, 1, 1.0f));
        NamakApplication.addToVolleyRequestQueue(dashboardRequest);
    }

    public static void onDashboardLoadFinished(JSONArray newDashboard) {
        if (newDashboard == null) {
            return;
        }
        for (int i = 0; i < newDashboard.length(); i++) {
            JSONObject dashboardJSON = null;
            String title = "Not Set Yet!";
            try {
                dashboardJSON = newDashboard.getJSONObject(i);
                title = dashboardJSON.optString("title");
                if (title == null || title.isEmpty()) {
                    title = dashboardJSON.getString("client") == "local_async" ? "Asynchronous" : "Synchronous";
                    title += " execution of " + dashboardJSON.getString("fun") + " on " + dashboardJSON.getString("tgt");
                    if (dashboardJSON.opt("arg") == null) {
                        title += " with no arguments";
                    } else {
                        title += " with following arguments: " + dashboardJSON.opt("arg").toString();
                    }
                    dashboardJSON.put("title", title);
                }

            } catch (JSONException error) {
//                Toast.makeText(NamakApplication.getAppContext(), "Failed to get title of dashboard item #" + i, Toast.LENGTH_SHORT).show();
                Log.e("NamaApp: title", error.toString(), error);
                Log.d("NamaApp: title", dashboardJSON.toString().substring(0, 50));
//                        mSwipeRefreshLayout.setEnabled(true);
                // FIXME Put some translated string and check it in the ListView and maybe Disable Click
                title = "Wrong / Disabled!";
            }
            NamakApplication.dashboard.add(dashboardJSON);
            dashboardListAdapter.add(title);
        }
    }

}
