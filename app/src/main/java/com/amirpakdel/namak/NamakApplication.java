package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

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
    private static int timeout = TimeoutPreference.DEFAULT_TIMEOUT;
    private static Activity foregroundActivity = null;

    private static SparseArray<JSONArray> dashboards;
    public static JSONObject getDashboardItem(int dashboard, int dashboardItemPosition) throws JSONException {
        return dashboards.get(dashboard).getJSONObject(dashboardItemPosition);
    }
    private static SparseArray<String> dashboardNames;
    public static SparseArray<JSONArray> getDashboards() {
        return dashboards;
    }
    public static String getDashboardName(int i) {
        return dashboardNames.valueAt(i);
    }


    private static final ArrayList<DashboardListener> mDashboardListeners = new ArrayList<>(2);
    private static DashboardAdapter dashboardAdapter;


    private static final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
        public void onActivityCreated(Activity activity, Bundle bundle) {
            foregroundActivity = activity;
        }

        @Override
        public void onActivityStarted(Activity activity) { /* No op */ }
        @Override
        public void onActivityResumed(Activity activity) { /* No op */ }
        @Override
        public void onActivityPaused(Activity activity) { /* No op */ }
        @Override
        public void onActivityStopped(Activity activity) { /* No op */ }
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) { /* No op */ }
        @Override
        public void onActivityDestroyed(Activity activity) { /* No op */ }
    }

    public static Context getAppContext() { return context; }

    public static SaltMaster getSaltMaster() { return sm; }

    public static SharedPreferences getPref() { return pref; }

    public static void loadSaltmasters() {
        Set<String> saltmasterStringSet = pref.getStringSet("saltmasters", new HashSet<String>());
        saltmasters = new String[saltmasterStringSet.size()];
        saltmasters = saltmasterStringSet.toArray(saltmasters);
    }
    public static String[] getSaltmasterNames() {
        if (saltmasters == null) {
            loadSaltmasters();
        }
        String[] ret = new String[saltmasters.length];
        for (int i=0; i<saltmasters.length; i++) {
            ret[i] = pref.getString("saltmaster_" + saltmasters[i] + "_name", context.getString(R.string.pref_master_name_default, i));
        }
        return ret;
    }
    public static String getSaltMasterId(int index) {
        return saltmasters[index];
    }
    public static int getSaltMasterIndex() {
        if (sm == null || sm.getId() == null || saltmasters == null) { return -1; }
        return Arrays.binarySearch(saltmasters, sm.getId());
    }

    public static DashboardAdapter getDashboardAdapter() {
        return dashboardAdapter;
    }

    public static void addToVolleyRequestQueue(Request req) {
        queue.add(req);
        // Expecting some idle time
        System.gc();
    }

    public static boolean getAutoExecute() {
        return mAutoExecute;
    }

    public void onCreate() {
        super.onCreate();
        // DEBUG
//      PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
//      PreferenceManager.setDefaultValues(this, R.xml.pref, true);
//        PreferenceManager.setDefaultValues(NamakApplication.context, R.xml.pref, false);

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        context = getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        prefChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                assert sm != null;

                if (key.equals("dashboards")) {
                    loadDashboards();
                    return;
                }
                if (key.startsWith("dashboard_") && key.endsWith("_url")) {
                    loadDashboard(key.substring(10, key.length() - 4));
                    return;
                }
                if (key.startsWith("dashboard_") && key.endsWith("_name")) {
                    dashboardNames.put(Integer.parseInt(key.substring(10, key.length() - 5)), pref.getString(key, null));
                    dashboardAdapter.notifyDataSetChanged();
                    return;
                }
                if (key.startsWith("saltmaster_"+sm.getId())) {
                    sm.login();
                    return;
                }

                switch (key) {
                    case "timeout":
                        timeout = pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT);
                        sm.setTimeout(timeout);
                        break;
                    case "auto_execute":
                        mAutoExecute = pref.getBoolean("auto_execute", false);
                        break;
//                    default:
//                        Log.d("prefChanged", "Not grabbing changes of this preference: " + key);
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(prefChanged);
        mAutoExecute = pref.getBoolean("auto_execute", false);
        timeout = pref.getInt("timeout", TimeoutPreference.DEFAULT_TIMEOUT);

        int dashboardsCount = pref.getStringSet("dashboards", new HashSet<String>()).size();
        dashboards = new SparseArray<>(dashboardsCount);
        dashboardNames = new SparseArray<>(dashboardsCount);

        // Volley
        queue = Volley.newRequestQueue(context);

        sm = new SaltMaster(timeout);

        dashboardAdapter = new DashboardAdapter(context);
        loadDashboards();
    }

    public static void addDashboardListener(DashboardListener dashboardListener) {
        mDashboardListeners.add(dashboardListener);
        dashboardListener.onDashboardLoadFinished();
    }

    public interface DashboardListener {
        void onDashboardLoadFinished();
    }

    private static int loadingDashboards = 0;
    private static void triggerDashboardListeners() {
        // FIXME race condition?
        loadingDashboards--;
        if (loadingDashboards>0) { return; }
        dashboardAdapter.notifyDataSetChanged();
        for (DashboardListener dashboardListener : mDashboardListeners) {
            dashboardListener.onDashboardLoadFinished();
        }
    }

    public static void loadDashboards() {
        loadDashboards(false);
    }
    public static void reloadDashboards() {
        loadDashboards(true);
    }
    public static void loadDashboards(boolean forceReload) {
        Set<String> dashboardsPref = pref.getStringSet("dashboards", new HashSet<String>());
        for (String dashboard : dashboardsPref) {
            if (forceReload || dashboards.indexOfKey(Integer.parseInt(dashboard)) < 0) {
                loadDashboard(dashboard);
            }
        }
        for(int i = 0; i < dashboards.size(); i++) {
            if ( ! dashboardsPref.contains(String.valueOf(dashboards.keyAt(i))) ) {
                dashboards.removeAt(i);
                dashboardNames.removeAt(i);
            }
        }
    }

    private static void loadDashboard(final String dashboard) {
        final String dashboardName = pref.getString("dashboard_" + dashboard + "_name", null);
        String dashboardUrl  = pref.getString("dashboard_" + dashboard + "_url", null);
        if (dashboardUrl == null) { return; }
        JsonArrayRequest dashboardRequest = new JsonArrayRequest(dashboardUrl,
                new Response.Listener<JSONArray>() {
                    public void onResponse(JSONArray response) {
                        parseDashboard(dashboard, dashboardName, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Popup.error(NamakApplication.foregroundActivity, context.getString(R.string.dashboard_load_failed_get, dashboardName), 100, error);
                        triggerDashboardListeners();
                    }
                });
        // To set or not to set the timeout, that is the question
        dashboardRequest.setRetryPolicy(new DefaultRetryPolicy(timeout * 1000, 1, 1.0f));
        NamakApplication.addToVolleyRequestQueue(dashboardRequest);
        // FIXME race condition?
        loadingDashboards++;
    }

    public static void parseDashboard(String dashboard, String dashboardName, JSONArray newDashboard) {
        if (newDashboard == null) {
            Popup.error(NamakApplication.foregroundActivity, context.getString(R.string.dashboard_load_null, dashboardName), 101, null);
            triggerDashboardListeners();
            return;
        }
        JSONException parseError = null;
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
                }
            } catch (JSONException error) {
                title = context.getString(R.string.dashboard_bad_item);
                parseError = error;
                Log.d("NamakApp: title", dashboardJSON.toString().substring(0, 50));
            }
            try {
                dashboardJSON.put("title", title);
            } catch (JSONException error) {
                Popup.error(NamakApplication.foregroundActivity, context.getString(R.string.should_never_happen), 103, error);
            }
        }
        dashboards.put(Integer.parseInt(dashboard), newDashboard);
        dashboardNames.put(Integer.parseInt(dashboard), dashboardName);
        if (parseError == null) {
            Popup.message(context.getString(R.string.dashboard_load_succeeded, dashboardName));
        } else {
            Popup.error(NamakApplication.foregroundActivity, context.getString(R.string.dashboard_load_failed_parse, dashboardName), 102, parseError);
        }
        triggerDashboardListeners();
    }

}
