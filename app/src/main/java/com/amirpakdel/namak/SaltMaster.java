package com.amirpakdel.namak;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

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

        Toast.makeText(NamakApplication.getAppContext(), "Logging into " + mName + " (" + mBaseUrl + ") as " + mUsername, Toast.LENGTH_SHORT).show();
        JSONObject loginPayload = new JSONObject();

        try {
            loginPayload.put("eauth", mEauth);
            loginPayload.put("username", mUsername);
            loginPayload.put("password", mPassword);
        } catch (JSONException error) {
//            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.should_never_happen), 401, error);

            Toast.makeText(NamakApplication.getAppContext(), "Could not generate the login payload!", Toast.LENGTH_SHORT).show();
            Log.e("SaltMaster: Err.login", error.toString(), error);
        }
        SaltRequest loginRequest = new SaltRequest(this, "login", loginPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mAuthToken = response.getJSONArray("return").getJSONObject(0).getString("token");
                            Toast.makeText(NamakApplication.getAppContext(), "Successfully logged into " + mName, Toast.LENGTH_LONG).show();
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

    public String getRelativeUrl(String path) {
        try {
            return new URL(new URL(mBaseUrl), path).toString();
        } catch (MalformedURLException error) {
            // FIXME Add proper error handling
            Log.e("SaltMaster", "getRelativeUrl: " + path + " is not a relative URL", error);
            return null;
        }
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
    public String getAuthToken() {
        return mAuthToken;
    }
    public String getName() {
        if (mAuthToken == null) {
            return NamakApplication.getAppContext().getString(R.string.not_logged_in);
        };
        return mName;
    }


    public String getBaseUrl() {
        return mBaseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.mBaseUrl = baseUrl;
        this.login();
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
}
