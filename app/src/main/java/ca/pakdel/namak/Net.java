package ca.pakdel.namak;

import android.annotation.SuppressLint;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class Net {
    @SuppressLint("StaticFieldLeak")
    private static Net instance;
    private RequestQueue requestQueue;

    private Net(Context context) {
        assert requestQueue == null;
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }


    static synchronized void init(Context context) {
        assert instance == null;
        instance = new Net(context);
    }

    static void addToRequestQueue(Request<?> req) {
        instance.requestQueue.add(req);
        // TODO Maybe?
        // Expecting some idle time
        // System.gc();
    }
}
