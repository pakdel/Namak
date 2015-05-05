package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;


public class CommandExecutionActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String COMMAND_ITEM_POSITION = "command_position";
    private Activity commandActivity;
    private JSONObject mJSONCommand;
    private String mJID;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout mExecutionLogLayout;
    private LinearLayout.LayoutParams mLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commandActivity = this;
        mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.setMargins(10, 30, 10, 30);

        TextView commandTextView = new LogView(this, mLayoutParams);

        try {
            mJSONCommand = NamakApplication.getDashboardItem(getIntent().getExtras().getInt(COMMAND_ITEM_POSITION));
            commandTextView.setText("Command: \n" + mJSONCommand.toString(2));
        } catch (JSONException e) {
            mJSONCommand = null;
            commandTextView.setText(e.toString());
        }

        mExecutionLogLayout = new LinearLayout(this);
        mExecutionLogLayout.setOrientation(LinearLayout.VERTICAL);
        mExecutionLogLayout.setPadding(10, 10, 10, 10);
        mExecutionLogLayout.addView(commandTextView);

        ScrollView executionLogScrollView = new ScrollView(this);
        executionLogScrollView.addView(mExecutionLogLayout);
        mSwipeRefreshLayout = new SwipeRefreshLayout(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(true);
//        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.addView(executionLogScrollView);
        setContentView(mSwipeRefreshLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mJSONCommand == null) {
            return;
        }

//        Toast.makeText(NamakApplication.getAppContext(), "Running '" + cmdName + "'", Toast.LENGTH_SHORT).show();
//        Log.d("CmdExec: cmd", mJSONCommand.toString(2));
        String cmdType;
        try {
            cmdType = mJSONCommand.getString("client");
        } catch (JSONException e) {
            Toast.makeText(NamakApplication.getAppContext(), "Failed get the command type!", Toast.LENGTH_SHORT).show();
            Log.e("CmdExec: cmd", e.toString(), e);
            return;
        }
        String execMsg;
        final boolean async;
        switch (cmdType) {
            case "local":
                execMsg = "Executing Synchronously...";
                async = false;
                break;
            case "local_async":
                execMsg = "Executing Asynchronously...";
                async = true;
                break;
            default:
                Toast.makeText(NamakApplication.getAppContext(), "Client " + cmdType + " is not supported!", Toast.LENGTH_SHORT).show();
                return;
        }

        SaltRequest cmdRequest = new SaltRequest(NamakApplication.getSaltMaster(), "", mJSONCommand,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CmdExec: cmd", response.toString(2));
                            TextView resultsTextView = new LogView(commandActivity, mLayoutParams);
                            if (async) {
                                mJID = response.getJSONArray("return").getJSONObject(0).getString("jid");
                                resultsTextView.setText("Job ID: " + mJID);
                                Toast.makeText(NamakApplication.getAppContext(), "Pull to check for the the results", Toast.LENGTH_SHORT).show();
                                mSwipeRefreshLayout.setEnabled(true);
//                                mSwipeRefreshLayout.setRefreshing(false);
                                Log.d("CmdExec: cmd", "JID: " + mJID);
                            } else {
//                                resultsTextView.setText("Results: " + response.toString(2));
                                resultsTextView.setText("Results: " + response.getJSONArray("return").toString(2));
                            }
                            mExecutionLogLayout.addView(resultsTextView);
                        } catch (JSONException error) {
                            Toast.makeText(NamakApplication.getAppContext(), "Failed to run the command!", Toast.LENGTH_SHORT).show();
                            Log.e("CmdExec: cmd", error.toString(), error);
                            Log.d("CmdExec: cmd", response.toString().substring(0, 50));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NamakApplication.getAppContext(), "Failed to run the command!", Toast.LENGTH_SHORT).show();
                        Log.e("Main: cmd.Err", error.toString(), error);
                    }
                });

        NamakApplication.addToVolleyRequestQueue(cmdRequest);
        TextView startedExecutionTextView = new LogView(this, mLayoutParams);
        startedExecutionTextView.setText(execMsg);
        mExecutionLogLayout.addView(startedExecutionTextView);
    }

    // TODO
    @Override
    public void onRefresh() {
//        Toast.makeText(NamakApplication.getAppContext(), "Getting result of job ID " + mJID, Toast.LENGTH_SHORT).show();
        SaltRequest jidRequest = new SaltRequest(NamakApplication.getSaltMaster(), "/jobs/" + mJID, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CmdExec: jid", response.toString(2));
                            TextView resultsTextView = new LogView(commandActivity, mLayoutParams);
//                            resultsTextView.setText("Results: " + response.toString(2));
                            resultsTextView.setText("Results: " + response.toString(2));

                            mExecutionLogLayout.addView(resultsTextView);

                            mSwipeRefreshLayout.setEnabled(false);
//                            mSwipeRefreshLayout.setRefreshing(false);
                        } catch (JSONException error) {
                            Toast.makeText(NamakApplication.getAppContext(), "Failed to get JID " + mJID, Toast.LENGTH_SHORT).show();
                            Log.e("CmdExec: jid", error.toString(), error);
                            Log.d("CmdExec: jid", response.toString().substring(0, 50));
                        }
//                        mSwipeRefreshLayout.setEnabled(true);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        mSwipeRefreshLayout.setEnabled(true);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(NamakApplication.getAppContext(), "Failed to run the command!", Toast.LENGTH_SHORT).show();
                        Log.e("Main: jid.Err", error.toString(), error);
                    }
                });

        // FIXME
        jidRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                        1,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NamakApplication.addToVolleyRequestQueue(jidRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
//            default:
//                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private class LogView extends TextView {
        public LogView(Context context, LinearLayout.LayoutParams layoutParams) {
            super(context);
            setLayoutParams(layoutParams);
//            setTextColor(Color.BLACK);
//            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }
    }
}
