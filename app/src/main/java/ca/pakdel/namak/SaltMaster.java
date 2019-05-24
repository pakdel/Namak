package ca.pakdel.namak;

// TODO error handling
//      look for try / catch

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceScreen;
import ca.pakdel.namak.settings.PreferenceScreenProvider;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public class SaltMaster implements PreferenceScreenProvider {
    public static final String ID = "id";
    static final String PREFERENCE_KEY = "saltmasters";
    static final String PREFERENCE_KEY_PREFIX = "saltmaster_";

    private String id;
    private @ColorInt int color;
    private String name;
    private String baseUrl;
    private String username;
    private String password;
    // List of valid auth_backend / eauth is set in R.array.auth_backends
    private String auth_backend;
    private String token;
    private long tokenExpiration;

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public enum States {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }
    private MutableLiveData<EnumSet<States>> state;
    LiveData<EnumSet<States>> getState() { return state; }


    SaltMaster(@NonNull SharedPreferences sharedPref, @NonNull String id) {
        this.id = id;
        color = sharedPref.getInt(PREFERENCE_KEY_PREFIX + id + "_color", Color.BLUE);
        name = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_name", null);
        baseUrl = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_url", null);
        username = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_username", null);
        password = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_password", null);
        auth_backend = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_eauth", null);

        // We trust the Preferences to be set properly
        // assert Arrays.asList(new String[]{name, baseUrl, username, password, auth_backend}).contains(null);
        // assert getResources().getStringArray(R.array.auth_backends).contains(auth_backend);

        token = null;
        tokenExpiration = 0;
        state = new MutableLiveData<>();
        state.setValue(EnumSet.of(States.DISCONNECTED));
    }

    // TODO Move to a ViewAdapter
    public void updatePreferenceFragment(Context context, PreferenceScreen screen) {
        EditTextPreference textPreference;

        textPreference = new EditTextPreference(context);
        textPreference.setTitle("Name");
        textPreference.setKey(PREFERENCE_KEY_PREFIX + getId() + "_name");
        textPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        screen.addPreference(textPreference);

        textPreference = new EditTextPreference(context);
        textPreference.setTitle("Base Url");
        textPreference.setKey(PREFERENCE_KEY_PREFIX + getId() + "_url");
        textPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        screen.addPreference(textPreference);

        textPreference = new EditTextPreference(context);
        textPreference.setTitle("Username");
        textPreference.setKey(PREFERENCE_KEY_PREFIX + getId() + "_username");
        textPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        screen.addPreference(textPreference);

        textPreference = new EditTextPreference(context);
        textPreference.setTitle("Password");
        textPreference.setKey(PREFERENCE_KEY_PREFIX + getId() + "_password");
        textPreference.setSummary("********");
        screen.addPreference(textPreference);

        ListPreference authBackendPreference = new ListPreference(context);
        authBackendPreference.setTitle("Authentication backend");
        authBackendPreference.setEntries(R.array.auth_backends);
        authBackendPreference.setEntryValues(R.array.auth_backends);
        authBackendPreference.setKey(PREFERENCE_KEY_PREFIX + getId() + "_eauth");
        authBackendPreference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        screen.addPreference(authBackendPreference);
    }

    void login() {
        if (token != null && tokenExpiration > System.currentTimeMillis()) {
            // Already logged in
            return;
        }
        assert state.getValue() != null;
        if (state.getValue().contains(States.CONNECTING)) {
            // Already logging in
            return;
        }

        token = null;
        tokenExpiration = 0;
        state.setValue(EnumSet.of(States.CONNECTING));

        JSONObject loginPayload = new JSONObject();
        try {
            loginPayload.put("eauth", auth_backend);
            loginPayload.put("username", username);
            loginPayload.put("password", password);
        } catch (JSONException e) {
            state.setValue(EnumSet.of(States.DISCONNECTED, States.ERROR));
            Popup.error(e);
            return;
        }

        JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, getBaseUrl() + "/login", loginPayload,
                response -> {
                    // response will be something like
                    // {"return":[{"perms":[".*","@runner"],"start":1.557938622279831E9,"token":"d2903f3f6f3a229577fcaff58bcd2bb8e9f736f0","expire":1.557981822279832E9,"user":"amirp","eauth":"pam"}]}
                    try {
                        token = response.getJSONArray("return").getJSONObject(0).getString("token");
                        tokenExpiration = response.getJSONArray("return").getJSONObject(0).getLong("expire");
                        state.setValue(EnumSet.of(States.CONNECTED));
                    } catch (JSONException e) {
                        state.setValue(EnumSet.of(States.DISCONNECTED, States.ERROR));
                        Popup.error(e);
                    }
                }, error -> { state.setValue(EnumSet.of(States.DISCONNECTED, States.ERROR)); Popup.error(error); }
        );
        // Access the RequestQueue through your singleton class.
        Net.addToRequestQueue(loginRequest);
    }

    class CommandExecutionRequest extends JsonObjectRequest {
        CommandExecutionRequest(JSONObject commandPayload, MutableLiveData<String> result) {
            super(getBaseUrl(), commandPayload,
                    response -> {
                        try {
                            result.setValue(response.toString(4));
                        } catch (JSONException e) {
                            Popup.error(e);
                            result.setValue(response.toString());
                        }
                    }, error -> { result.setValue("Failed!"); Popup.error(error); }
            );
        }

        @Override
        public Map<String, String> getHeaders() {
            // Map<String, String> headers = super.getHeaders();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            assert token != null;
            headers.put("X-Auth-Token", token);
            return headers;
        }
    }

    void execute(Command command, MutableLiveData<String> result) {
        JSONObject commandPayload = command.getPayload();
        try {
            commandPayload.put("token", token);
        } catch (JSONException e) {
            Popup.error(e);
        }

        JsonObjectRequest jsonObjectRequest = new CommandExecutionRequest(commandPayload, result);
        // Access the RequestQueue through your singleton class.
        Net.addToRequestQueue(jsonObjectRequest);
    }
}