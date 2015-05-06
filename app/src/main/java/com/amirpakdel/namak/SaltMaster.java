package com.amirpakdel.namak;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SaltMaster {
    //    private DashboardListener dashboardListener = null;
    // Currently we have 2 DashboardListeners:
    // - NamakApplication which updates dashboardListAdapter
    // - MainActivity which stops the refreshing animation (setRefreshing false)
    private final ArrayList<DashboardListener> mDashboardListeners = new ArrayList<>(2);
    /**
     * Created by pakdel@gmail.com on 23/04/15.
     */
    private String mBaseUrl;
    private String mDashboardUrl;
    private String mUsername;
    private String mEauth;
    private String mPassword;
    private int mTimeout;
    private String mAuthToken;
    private JSONObject mDashboard;

    public SaltMaster(String baseUrl, String dashboardUrl, String eauth, String username, String password, int timeout, DashboardListener dashboardListener) {
        this.mBaseUrl = baseUrl;
        this.mDashboardUrl = dashboardUrl;
        this.mEauth = eauth;
        this.mUsername = username;
        this.mPassword = password;
        this.mTimeout = timeout;
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
            loginPayload.put("eauth", mEauth);
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
        JsonObjectRequest dashboardRequest = new JsonObjectRequest(getDashboardFullUrl(mDashboardUrl), null,
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
        // To set or not to set the timeout, that is the question
        dashboardRequest.setRetryPolicy(new DefaultRetryPolicy(mTimeout * 1000, 1, 1.0f));
        NamakApplication.addToVolleyRequestQueue(dashboardRequest);
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.mBaseUrl = baseUrl;
        this.login();
    }

    private String getDashboardFullUrl(String dashboardUrl) {
        try {
            return new URL(dashboardUrl).toString();
        } catch (MalformedURLException e) {
            // It's fine if dashboardUrl is not a full URL
//            Log.e("SaltMaster: dashboard", dashboardUrl + " is not a URL", e);
        }
        try {
            return new URL(new URL(mBaseUrl), dashboardUrl).toString();
        } catch (MalformedURLException e) {
            Log.e("SaltMaster: dashboard", mBaseUrl + " is not a URL", e);
        }
        return mBaseUrl + dashboardUrl;
    }
//    public String getDashboardUrl() { return mDashboardUrl; }
    public void setDashboardUrl(String dashboardUrl) {
        this.mDashboardUrl = dashboardUrl;
        this.loadDashboard();
    }

    //    public String getEauth() { return mEauth; }
    //    public String getUsername() { return mUsername; }
    //    public String getPassword() { return mPassword; }
    public String getAuthToken() {
        return mAuthToken;
    }

    public void setEauth(String eauth) {
        this.mEauth = eauth;
        this.login();
    }

    public void setUsername(String username) {
        this.mUsername = username;
        this.login();
    }

    public void setPassword(String password) {
        this.mPassword = password;
        this.login();
    }

    public int getTimeout() {
        return mTimeout;
    }

    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
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
