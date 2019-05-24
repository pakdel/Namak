package ca.pakdel.namak;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.ColorInt;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


class CommandAndColor extends Command {
    public @ColorInt int color;
    CommandAndColor(JSONObject commandJSON, int dashboardColor) {
        super(commandJSON);
        color = dashboardColor;
    }
}


public class Dashboard {
    static final String COMMAND_POSITION = "command_position";
    static final String PREFERENCE_KEY = "dashboards";
    static final String PREFERENCE_KEY_PREFIX = "dashboard_";

    private String id;
    private @ColorInt int color;
    private String name;
    private String Url;
    private List<CommandAndColor> commands = new ArrayList<>();

    private Dashboard(SharedPreferences sharedPref, String id) {
        this.id = id;
        color = sharedPref.getInt(PREFERENCE_KEY_PREFIX + id + "_color", Color.TRANSPARENT);
        // color = Color.valueOf(color);
        name = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_name", null);
        Url = sharedPref.getString(PREFERENCE_KEY_PREFIX + id + "_url", null);
    }

    static CompletableFuture<Dashboard> loadAsync(SharedPreferences sharedPref, String id) {
        CompletableFuture<Dashboard> newDashboardFuture = new CompletableFuture<>();
        Dashboard newDashboard = new Dashboard(sharedPref, id);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(newDashboard.Url,
                response -> {
                        for (int i = 0; i < response.length(); i++) {
                            newDashboard.commands.add(new CommandAndColor(response.optJSONObject(i), newDashboard.color));
                        }
                    newDashboardFuture.complete(newDashboard);
                }, error -> { Log.d("Dashboard.jsonObjectRequest", "onErrorResponse: " + error.toString()); newDashboardFuture.complete(newDashboard);}
        );
        // Access the RequestQueue through a singleton
        Net.addToRequestQueue(jsonArrayRequest);
        return newDashboardFuture;
    }


    int count() {
        return commands.size();
    }

    CommandAndColor get(int i) {
        return commands.get(i);
    }

    public String getId() {
        return id;
    }

    public @ColorInt int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return Url;
    }
}
