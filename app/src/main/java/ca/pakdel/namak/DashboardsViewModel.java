package ca.pakdel.namak;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;



public class DashboardsViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPref;
    private MutableLiveData<List<Dashboard>> dashboards;


    static CommandAndColor getCommandAndColor(@NonNull List<Dashboard> dashboards, int position) {
        for (Dashboard dashboard: dashboards) {
            if (position < dashboard.count()) {
                return dashboard.get(position);
            }
            position -= dashboard.count();
        }
        return null;
    }
    CommandAndColor getCommandAndColor(int position) {
        assert dashboards != null;
        assert dashboards.getValue() != null;
        return getCommandAndColor(dashboards.getValue(), position);
    }

    public DashboardsViewModel(@NonNull Application application) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    // This is not supposed to happen often,
    // so we are blindly reloading everything (no optimization)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith(Dashboard.PREFERENCE_KEY_PREFIX)) {
            reloadDashboards();
        }
    }

    @NonNull
    public MutableLiveData<List<Dashboard>> getDashboards() {
        if (dashboards == null) {
            dashboards = new MutableLiveData<>();
            dashboards.setValue(new ArrayList<>());
            reloadDashboards();
        }
        return dashboards;
    }

    // sharedPref.getStringSet(...) is not null
    @SuppressWarnings("ConstantConditions")
    private void reloadDashboards() {
        if (! sharedPref.contains(Dashboard.PREFERENCE_KEY)) {
            dashboards.setValue(new ArrayList<>());
            return;
        }
        // We can use CompletableFuture instead of AsyncTask
        // new LoadDashboardsAsyncTask(sharedPref, dashboards).execute();
        CompletableFuture.supplyAsync(() -> sharedPref.getStringSet(Dashboard.PREFERENCE_KEY, new HashSet<>())
                .parallelStream()
                .map(dashboardId -> Dashboard.loadAsync(sharedPref, dashboardId))
                .map(CompletableFuture::join)
                .collect(Collectors.toList())
        ).thenAcceptAsync(result -> dashboards.postValue(result));
/*
                .handleAsync( (s,t) -> {
                    Log.d("DashboardsViewModel", "reloadDashboards: CompletableFuture handleAsync arg 1: " + s);
                    Log.d("DashboardsViewModel", "reloadDashboards: CompletableFuture handleAsync arg 2: " + t);
                    return t;});
*/


    }

/*
    static private class LoadDashboardsAsyncTask extends AsyncTask<Void, Void, List<Dashboard>> {
        MutableLiveData<List<Dashboard>> dashboards;
        List<CompletableFuture<Dashboard>> futureDashboardList;
        LoadDashboardsAsyncTask(SharedPreferences sharedPref, MutableLiveData<List<Dashboard>> dashboards) {
            this.dashboards = dashboards;
            assert sharedPref.contains("dashboards");
            futureDashboardList = sharedPref.getStringSet("dashboards", new HashSet<>())
                    .stream()
                    .map(dashboardId -> Dashboard.loadAsync(sharedPref, dashboardId))
                    .collect(Collectors.toList());
        }

        @Override
        protected List<Dashboard> doInBackground(Void... voids) {
            // Wait for all Dashboards to load
            return futureDashboardList
                    .stream()
                    .map(CompletableFuture::join)
                    // .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        @Override
        protected void onPostExecute(List<Dashboard> result) {
            dashboards.setValue(result);
        }
    }
*/

}
