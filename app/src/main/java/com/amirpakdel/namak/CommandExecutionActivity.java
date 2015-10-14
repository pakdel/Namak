package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
// TODO Translate commandMsg

public class CommandExecutionActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String COMMAND_GROUP_POSITION = "command_group_position";
    public static final String COMMAND_CHILD_POSITION = "command_child_position";
    static final int COMMAND_ADJUST_REQUEST = 0;
    private JSONObject mJSONCommand;
    private Boolean runner;
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

        mExecutionLogView = new LogView(this);
        try {
            mJSONCommand = NamakApplication.getDashboardItem(
                    getIntent().getExtras().getInt(COMMAND_GROUP_POSITION),
                    getIntent().getExtras().getInt(COMMAND_CHILD_POSITION));
            prepareCommand();
        } catch (JSONException error) {
            Popup.error(this, getString(R.string.should_never_happen), 600, error);
            assert mJSONCommand == null;
            assert async == null;
            return;
        }
        setupViews();
    }

    private void prepareCommand() {
        assert mJSONCommand != null;
        try {
            switch (mJSONCommand.getString("client")) {
                case "runner":
                    runner = true;
                    async = false;
                    break;
                case "local":
                    runner = false;
                    async = false;
                    break;
                case "local_async":
                    runner = false;
                    async = true;
                    break;
                default:
                    async = null;
                    mExecutionLogView.setError(this, getString(R.string.wrong_client, mJSONCommand.getString("client")), 602, null);
                    // mJSONCommand != null
                    // async == null
                    return;
            }
        } catch (JSONException error) {
            mExecutionLogView.setError(this, getString(R.string.no_client), 601, error);
            assert mJSONCommand != null;
            assert async == null;
            return;
        }

        String fun;
        try {
            fun = mJSONCommand.getString("fun");
        } catch (JSONException error) {
            mExecutionLogView.setError(this, getString(R.string.no_fun), 603, error);
            assert mJSONCommand != null;
            assert async != null;
            mJSONCommand = null;
             return;
        }
        if (runner) {
            mExecutionLogView.setText(fun, mJSONCommand.optJSONArray("args"));
        } else {
            try {
                mExecutionLogView.setText(fun, mJSONCommand.getString("tgt"), mJSONCommand.optString("expr_form", "glob"), mJSONCommand.optJSONArray("arg"));
            } catch (JSONException error) {
                mExecutionLogView.setError(this, getString(R.string.no_tgt), 604, error);
                assert mJSONCommand != null;
                assert async != null;
                mJSONCommand = null;
                // return;
            }
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
            final LinearLayout headerButtons = new LinearLayout(this);

//            final Resources r = getResources();
//            layoutParams.setMargins((int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin), (int) r.getDimension(R.dimen.activity_horizontal_margin), (int) r.getDimension(R.dimen.activity_vertical_margin));
//            headerButtons.setLayoutParams(layoutParams);

            final NamakButton modifyButton = new NamakButton(this);
            modifyButton.setText(R.string.modify);
            modifyButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(NamakApplication.getAppContext(), CommandModificationActivity.class);
                    intent.putExtra(CommandModificationActivity.COMMAND_JSON, mJSONCommand.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, COMMAND_ADJUST_REQUEST);
                }
            });
            headerButtons.addView(modifyButton);

            final Divider divider = new Divider(this);
            headerButtons.addView(divider);

            final NamakButton executeButton = new NamakButton(this);
            executeButton.setText(R.string.execute);
            executeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mExecutionResultsView.removeHeaderView(headerButtons);
                    execute();
                }
            });
            headerButtons.addView(executeButton);

            mExecutionResultsView.addHeaderView(headerButtons);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMMAND_ADJUST_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    mJSONCommand = new JSONObject(data.getExtras().getString(CommandModificationActivity.COMMAND_JSON));
                    prepareCommand();
                } catch (JSONException error) {
                    Popup.error(this, getString(R.string.should_never_happen), 611, error);
                }
//            } else {
//                Popup.message(String.format("Nothing has changed: %d / %d", requestCode, resultCode));
            }
            return;
        }
        Popup.error(this, getString(R.string.should_never_happen), 610, null);
    }

    private void execute() {
        SaltRequest cmdRequest = new SaltRequest(NamakApplication.getSaltMaster(), "", mJSONCommand,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (async) {
                                mJID = response.getJSONArray("return").getJSONObject(0).getString("jid");
                                mExecutionLogView.gotJID();
                                mSwipeRefreshLayout.setEnabled(true);
                                Popup.message(getString(R.string.pull_for_results));
                            } else {
                                mExecutionResultsListAdapter.setData(response.getJSONArray("return").getJSONObject(0));
                                mExecutionLogView.finished();
                            }
                        } catch (JSONException error) {
                            mExecutionLogView.finished();
                            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.exec_unexpected_response), 621, error);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mExecutionLogView.finished();
                        Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.exec_failed), 620, error);
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
                            mExecutionResultsListAdapter.setData(response.getJSONArray("return").getJSONObject(0));
                            mExecutionLogView.finished();
                            mSwipeRefreshLayout.setEnabled(false);
                        } catch (JSONException error) {
                            Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.job_unexpected_response), 623, error);
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
                        Popup.error(NamakApplication.getForegroundActivity(), NamakApplication.getAppContext().getString(R.string.job_failed), 622, error);
                    }
                });
        NamakApplication.addToVolleyRequestQueue(jidRequest);
    }

    private class LogView extends TextView {
        private String commandMsg;
        private Spannable commandText;

        public LogView(Context context) {
            super(context);
        }

        // This is only used for error messages
        public void setError(@NonNull final Activity parent, @NonNull CharSequence text, final int code, @Nullable Throwable error) {
            Popup.error(parent, text, code, error);
            // Having the "error" icon shown by setError(text) is enough
            // setTextColor(Color.RED);
            setText(text);
            setError(text);
            // requestFocus changes nothing
            // requestFocus();
        }

        public void setText(/*Boolean async,*/ @NonNull String fun, @Nullable JSONArray args) {
            assert async != null;

            commandMsg = "Execution of " + fun + " runner";
            if (args == null) {
                commandMsg += " with no arguments";
            } else {
                commandMsg += " with following arguments: " + args.toString();
            }

            commandText = new SpannableString(commandMsg);
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), 13, 13 + fun.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            setText(commandText, TextView.BufferType.SPANNABLE);
        }
        public void setText(/*Boolean async,*/ @NonNull String fun, @NonNull String tgt, @NonNull String matcher, @Nullable JSONArray arg) {
            assert async != null;

            commandMsg = async ? "Asynchronous" : "Synchronous";
            commandMsg += " execution of " + fun + " on " + tgt + " (" + matcher + ")";
            if (arg == null) {
                commandMsg += " with no arguments";
            } else {
                commandMsg += " with following arguments: " + arg.toString();
            }

            commandText = new SpannableString(commandMsg);
            int offset = async ? 12 : 11;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, async ? 12 : 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += 14;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), offset, offset + fun.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += fun.length() + 4;
            commandText.setSpan(new ForegroundColorSpan(Color.BLACK), offset, offset + tgt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
