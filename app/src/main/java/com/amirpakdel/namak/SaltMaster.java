package com.amirpakdel.namak;

import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class SaltMaster {
    private int mTimeout;
    private String mId;
    private String mName;
    private String mBaseUrl;
    private String mUsername;
    private String mPassword;
    private String mEauth;
    private String mAuthToken;

    public SaltMaster(int timeout) {
        this.mTimeout = timeout;
        this.mId = null;

        this.mName = null;
        this.mBaseUrl = null;
        this.mUsername = null;
        this.mPassword = null;
        this.mEauth = null;
        this.mAuthToken = null;
    }

    public void setId(String id) {
        this.mId = id;
        this.login();
    }

    public String getId() {
        return mId;
    }

    public void login() {

        SharedPreferences pref = NamakApplication.getPref();

        this.mName = pref.getString("saltmaster_" + mId + "_name", null);
        this.mBaseUrl = pref.getString("saltmaster_" + mId + "_url", null);
        this.mUsername = pref.getString("saltmaster_" + mId + "_username", null);
        this.mPassword = pref.getString("saltmaster_" + mId + "_password", null);
        this.mEauth = pref.getString("saltmaster_" + mId + "_eauth", null);

        this.mAuthToken = null;

        Popup.message(NamakApplication.getAppContext().getString(R.string.logging_in, mName, mUsername));
        JSONObject loginPayload = new JSONObject();

        try {
            loginPayload.put("eauth", mEauth);
            loginPayload.put("username", mUsername);
            loginPayload.put("password", mPassword);
        } catch (JSONException error) {
            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.should_never_happen), 401, error);
        }
        SaltRequest loginRequest = new SaltRequest(this, "login", loginPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mAuthToken = response.getJSONArray("return").getJSONObject(0).getString("token");
                            Popup.message(NamakApplication.getAppContext().getString(R.string.logged_in, mName));
                            NamakApplication.triggerSaltMasterListeners();
                        } catch (JSONException error) {
                            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.log_in_unexpected_response), 402, error);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mAuthToken = null;
                        Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.log_in_failed), 403, error);
                    }
                });
        NamakApplication.addToVolleyRequestQueue(loginRequest);
    }

    public String getRelativeUrl(String path) {
        try {
            return new URL(new URL(mBaseUrl), path).toString();
        } catch (MalformedURLException error) {
            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.should_never_happen), 404, error);
            return null;
        }
    }

//    private String getDashboardFullUrl(String dashboardUrl) {
//        try {
//            return new URL(dashboardUrl).toString();
//        } catch (MalformedURLException e) {
//            // It's fine if dashboardUrl is not a full URL
////            Log.e("SaltMaster: dashboard", dashboardUrl + " is not a URL", e);
//        }
//        try {
//            return new URL(new URL(mBaseUrl), dashboardUrl).toString();
//        } catch (MalformedURLException e) {
//            Log.e("SaltMaster: dashboard", mBaseUrl + " is not a URL", e);
//        }
//        return mBaseUrl + dashboardUrl;
//    }
    public String getAuthToken() {
        return mAuthToken;
    }
    public String getName() {
        if (mAuthToken == null) {
            return NamakApplication.getAppContext().getString(R.string.not_logged_in);
        }
        return mName;
    }


    public String getBaseUrl() {
        return mBaseUrl;
    }

//    public void setBaseUrl(String baseUrl) {
//        this.mBaseUrl = baseUrl;
//        this.login();
//    }
//
//    public void setEauth(String eauth) {
//        this.mEauth = eauth;
//        this.login();
//    }
//
//    public void setUsername(String username) {
//        this.mUsername = username;
//        this.login();
//    }
//
//    public void setPassword(String password) {
//        this.mPassword = password;
//        this.login();
//    }

    public int getTimeout() {
        return mTimeout;
    }

    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }
}
