package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//public class CommandExecutionActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
public class CommandExecutionActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String COMMAND_GROUP_POSITION = "command_group_position";
    public static final String COMMAND_CHILD_POSITION = "command_child_position";
    private JSONObject mJSONCommand;
    private Boolean async;
    private String mJID;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LogView mExecutionLogView;
    private ExpandableListView mExecutionResultsView;
    private ExecutionResultsBaseExpandableListAdapter mExecutionResultsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prepareCommand();
        setupViews();
    }


    private void prepareCommand() {
        try {
            mJSONCommand = NamakApplication.getDashboardItem(
                    getIntent().getExtras().getInt(COMMAND_GROUP_POSITION),
                    getIntent().getExtras().getInt(COMMAND_CHILD_POSITION));
        } catch (JSONException error) {
            // This should never happen
            mExecutionLogView = new LogView(this,
                    "Failed to find Dashboard Item "
                    + getIntent().getExtras().getInt(COMMAND_GROUP_POSITION)
                    + " / " + getIntent().getExtras().getInt(COMMAND_CHILD_POSITION));
            Log.e("CmdExec: onCreate", error.toString(), error);
            assert mJSONCommand == null;
            assert async == null;
            return;
        }

        try {
            switch (mJSONCommand.getString("client")) {
                case "local":
                    async = false;
                    break;
                case "local_async":
                    async = true;
                    break;
                default:
                    async = null;
                    mExecutionLogView = new LogView(this, "Client type '" + mJSONCommand.getString("client") + "' is not supported!");
                    // mJSONCommand != null
                    // async == null
                    return;
            }
        } catch (JSONException error) {
            mExecutionLogView = new LogView(this, "Failed to find the client type!");
            Log.e("CmdExec: onCreate", error.toString(), error);
            assert mJSONCommand != null;
            assert async == null;
            return;
        }

        try {
            mExecutionLogView = new LogView(this, mJSONCommand.getString("fun"), mJSONCommand.getString("tgt"), mJSONCommand.optJSONArray("arg"));
        } catch (JSONException error) {
            mExecutionLogView = new LogView(this, "Failed to find either fun or tgt: " + error.toString());
            Log.e("CmdExec: onCreate", error.toString(), error);
            assert mJSONCommand != null;
            assert async != null;
            mJSONCommand = null;
            // return;
        }
    }

    private void setupViews() {
        Resources r = getResources();

        mExecutionResultsView = new ExpandableListView(this);
        ViewGroup.MarginLayoutParams executionResultsMargins = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        executionResultsMargins.setMargins((int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin), (int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin));
        mExecutionResultsView.setLayoutParams(executionResultsMargins);
        // The header is not Selectable
        // http://developer.android.com/reference/android/widget/ListView.html#addHeaderView(android.view.View, java.lang.Object, boolean)
        mExecutionResultsView.addHeaderView(mExecutionLogView, null, false);
        mExecutionResultsListAdapter = new ExecutionResultsBaseExpandableListAdapter(this);
        mExecutionResultsView.setAdapter(mExecutionResultsListAdapter);
//        mExecutionResultsView.setPadding((int) r.getDimension(R.dimen.activity_horizontal_padding), (int) r.getDimension(R.dimen.activity_vertical_padding), (int) r.getDimension(R.dimen.activity_horizontal_padding), (int) r.getDimension(R.dimen.activity_vertical_padding));

        mSwipeRefreshLayout = new SwipeRefreshLayout(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(false);
//        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.addView(mExecutionResultsView);
        mSwipeRefreshLayout.setPadding((int) r.getDimension(R.dimen.activity_horizontal_padding), (int) r.getDimension(R.dimen.activity_vertical_padding), (int) r.getDimension(R.dimen.activity_horizontal_padding), (int) r.getDimension(R.dimen.activity_vertical_padding));
        setContentView(mSwipeRefreshLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mJSONCommand == null || async == null) {
            return;
        }

        if (NamakApplication.getAutoExecute()) {
            execute();
        } else {
            final Button executeButton = new Button(this);
            executeButton.setText("Execute");
            executeButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                mExecutionResultsView.removeHeaderView(executeButton);
                execute();
            } });
            mExecutionResultsView.addHeaderView(executeButton);
        }
    }

    private void execute() {
        SaltRequest cmdRequest = new SaltRequest(NamakApplication.getSaltMaster(), "", mJSONCommand,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CmdExec: cmd", response.toString(2));
                            if (async) {
                                mJID = response.getJSONArray("return").getJSONObject(0).getString("jid");
                                Log.d("CmdExec: cmd", "JID: " + mJID);
                                mExecutionLogView.gotJID();
                                mSwipeRefreshLayout.setEnabled(true);
                                Toast.makeText(NamakApplication.getAppContext(), "Pull to check for the the results", Toast.LENGTH_SHORT).show();
                            } else {
                                mExecutionResultsListAdapter.setData(response.getJSONArray("return").getJSONObject(0));
                                mExecutionLogView.finished();
                            }
                        } catch (JSONException error) {
                            mExecutionLogView.finished();
                            Toast.makeText(NamakApplication.getAppContext(), "Failed to run the command!", Toast.LENGTH_SHORT).show();
                            Log.e("CmdExec: cmd", error.toString(), error);
                            Log.d("CmdExec: cmd", response.toString().substring(0, 50));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mExecutionLogView.finished();
                        Toast.makeText(NamakApplication.getAppContext(), "Failed to run the command!", Toast.LENGTH_SHORT).show();
                        Log.e("Main: cmd.Err", error.toString(), error);
                    }
                });

        NamakApplication.addToVolleyRequestQueue(cmdRequest);
        mExecutionLogView.started();
    }

    @Override
    public void onRefresh() {
        SaltRequest jidRequest = new SaltRequest(NamakApplication.getSaltMaster(), "/jobs/" + mJID, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("CmdExec: jid", response.toString(2));
                            mExecutionResultsListAdapter.setData(response.getJSONArray("return").getJSONObject(0));
                            mExecutionLogView.finished();
                            mSwipeRefreshLayout.setEnabled(false);
                        } catch (JSONException error) {
                            Toast.makeText(NamakApplication.getAppContext(), "Failed to get JID " + mJID, Toast.LENGTH_SHORT).show();
                            Log.e("CmdExec: jid", error.toString(), error);
                            Log.d("CmdExec: jid", response.toString().substring(0, 50));
//                        mSwipeRefreshLayout.setEnabled(true);
                        }
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
        NamakApplication.addToVolleyRequestQueue(jidRequest);
    }

    private class LogView extends TextView {
        private String commandMsg;
        private Spannable commandText;

        // This is only used for error messages
        public LogView(Context context, @NonNull String error) {
            super(context);
            setTextColor(Color.RED);
            setText(error);
        }

        public LogView(Context context /*, Boolean async*/, @NonNull String fun, @NonNull String tgt, @Nullable JSONArray arg) {
            super(context);
            assert async != null;

//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            Resources r = getResources();
//            layoutParams.setMargins((int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin), (int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin));
//            setLayoutParams(layoutParams);

            commandMsg = async ? "Asynchronous" : "Synchronous";
            commandMsg += " execution of " + fun + " on " + tgt;
            if (arg == null) {
                commandMsg += " with no arguments";
            } else {
                commandMsg += " with following arguments: " + arg.toString();
            }

            commandText = new SpannableString(commandMsg);
//            commandText.setSpan(new RelativeSizeSpan(1.5f), 0, async ? 12 : 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            int offset = async ? 12 : 11;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, async ? 12 : 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += 14;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), offset, offset + fun.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += fun.length() + 4;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), offset, offset + tgt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            setTextColor(Color.BLACK);
//            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            setText(commandText, TextView.BufferType.SPANNABLE);
        }

        public void started() {
            setText(TextUtils.concat(commandText, new SpannableString(async ? "\nExecuting Asynchronously..." : "\nExecuting Synchronously...")), TextView.BufferType.SPANNABLE);
        }

        public void gotJID() {
            setText(TextUtils.concat(commandText, new SpannableString("\nJob ID: " + mJID)), TextView.BufferType.SPANNABLE);
        }

        public void finished() {
            setText(commandText, TextView.BufferType.SPANNABLE);
        }
    }
}
