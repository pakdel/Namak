package com.amirpakdel.namak;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SaltMaster {
    /**
     * Created by pakdel@gmail.com on 23/04/15.
     */
    private String mBaseUrl;
    private String mUsername;
    private String mPassword;
    private String mAuthToken;

    private JSONObject mDashboard;
    //    private DashboardListener dashboardListener = null;
    // Currently we have 2 DashboardListeners:
    // - NamakApplication which updates dashboardListAdapter
    // - MainActivity which stops the refreshing animation (setRefreshing false)
    private ArrayList<DashboardListener> mDashboardListeners = new ArrayList<>(2);

    public SaltMaster(String baseUrl, String username, String password, DashboardListener dashboardListener) {
        this.mBaseUrl = baseUrl;
        this.mUsername = username;
        this.mPassword = password;
//        this.mDashboardListeners.clear();
        this.mDashboardListeners.add(dashboardListener);
        this.mAuthToken = null;
        this.mDashboard = null;
        this.login();
    }

    private void triggerDashboardListeners() {
        for (DashboardListener dashboardListener : mDashboardListeners) {
            dashboardListener.onDashboardLoadFinished(mDashboard);
        }
    }


    private void login() {
        Toast.makeText(NamakApplication.getAppContext(), "Logging into " + mBaseUrl + " as " + mUsername, Toast.LENGTH_SHORT).show();
        JSONObject loginPayload = new JSONObject();

        try {
            loginPayload.put("eauth", "ldap");
            loginPayload.put("username", mUsername);
            loginPayload.put("password", mPassword);
        } catch (JSONException error) {
            Toast.makeText(NamakApplication.getAppContext(), "Could not generate the login payload!", Toast.LENGTH_SHORT).show();
            Log.e("SaltMaster: Err.login", error.toString(), error);
        }
        SaltRequest loginRequest = new SaltRequest(this, "login", loginPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mAuthToken = response.getJSONArray("return").getJSONObject(0).getString("token");
                            loadDashboard();
                            Log.d("SaltMaster: login", "authToken = " + mAuthToken);
                        } catch (JSONException error) {
                            Toast.makeText(NamakApplication.getAppContext(), "Unexpected response during login!", Toast.LENGTH_SHORT).show();
                            Log.e("SaltMaster: login", error.toString(), error);
                            Log.d("SaltMaster: login", response.toString().substring(0, 50));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mAuthToken = null;
                        Toast.makeText(NamakApplication.getAppContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                        Log.e("SaltMaster: login.Err", error.toString(), error);
                    }
                });
        NamakApplication.addToVolleyRequestQueue(loginRequest);
    }

    public void loadDashboard() {
        if (mAuthToken == null) {
            Toast.makeText(NamakApplication.getAppContext(), "Not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        SaltRequest dashboardRequest = new SaltRequest(this, "static/dashboard.json", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mDashboard = response.getJSONObject(mUsername);
                            Log.d("SaltMaster: dashboard", mDashboard.toString(2).substring(0, 50));
                            Toast.makeText(NamakApplication.getAppContext(), "Loaded dashboard of " + mUsername, Toast.LENGTH_SHORT).show();
                        } catch (JSONException error) {
                            mDashboard = null;
                            Toast.makeText(NamakApplication.getAppContext(), "Unexpected response when getting dashboard!", Toast.LENGTH_SHORT).show();
                            Log.e("SaltMaster: login", error.toString(), error);
                            Log.d("SaltMaster: dashboard", response.toString().substring(0, 50));
                        }
                        triggerDashboardListeners();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NamakApplication.getAppContext(), "Failed to load the dashboard!", Toast.LENGTH_SHORT).show();
                        Log.e("SaltMaster: dash.Err", error.toString(), error);
                        triggerDashboardListeners();
                    }
                });
        NamakApplication.addToVolleyRequestQueue(dashboardRequest);

    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public void setBaseUrl(String mBaseUrl) {
        this.mBaseUrl = mBaseUrl;
        this.login();
    }

    //    public String getUsername() { return mUsername; }
//    public String getPassword() { return mPassword; }
    public String getAuthToken() {
        return mAuthToken;
    }

    public void setUsername(String username) {
        this.mUsername = username;
        this.login();
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
        this.login();
    }

    //    public void setAuthToken(String mAuthToken) { this.mAuthToken = mAuthToken; }
    public void addDashboardListener(DashboardListener dashboardListener) {
        this.mDashboardListeners.add(dashboardListener);
        dashboardListener.onDashboardLoadFinished(mDashboard);
    }

    public interface DashboardListener {
        // If loading the mDashboard has not been successful, mDashboard will be null
        void onDashboardLoadFinished(JSONObject dashboard);
    }
}
