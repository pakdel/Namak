package com.amirpakdel.namak;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class SaltRequest extends JsonObjectRequest {
    private static final Response.Listener<JSONObject> defaultListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.d("SaltReq: Resp", response.toString(2).substring(0, 50));
            } catch (JSONException e) {
                Log.d("SaltReq: Resp", response.toString().substring(0, 50));
            }
        }
    };
    private static final Response.ErrorListener defaultErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("SaltReq: Err.Resp", error.toString(), error);
        }
    };
    private final String mAuthToken;

    public SaltRequest(SaltMaster sm, String api, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(sm.getRelativeUrl(api), jsonRequest,
                (listener != null) ? listener : defaultListener,
                (errorListener != null) ? errorListener : defaultErrorListener);
        mAuthToken = sm.getAuthToken();
        setRetryPolicy(new DefaultRetryPolicy(sm.getTimeout() * 1000, 1, 1.0f));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        if (mAuthToken != null) {
            headers.put("X-Auth-Token", mAuthToken);
        }
        return headers;
    }
}
