package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommandModificationActivity extends AppCompatActivity {

    public static final String COMMAND_JSON = "command_json";
    private int horizontal_padding, /*horizontal_padding_large,*/
            vertical_padding, vertival_margin;

    private JSONObject mJSONCommand;
    boolean runner;
    boolean async;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Resources r = getResources();
        horizontal_padding = (int) r.getDimension(R.dimen.activity_horizontal_padding);
        vertical_padding = (int) r.getDimension(R.dimen.activity_vertical_padding);
        vertival_margin = (int) r.getDimension(R.dimen.activity_vertical_margin);

        final LinearLayout commandLayout = new LinearLayout(this);
        commandLayout.setOrientation(LinearLayout.VERTICAL);
        commandLayout.setPadding(horizontal_padding, vertical_padding, horizontal_padding, vertical_padding);

        try {
            mJSONCommand = new JSONObject(getIntent().getExtras().getString(COMMAND_JSON));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, vertival_margin, 0, 0);

            final Switch client = new Switch(this);
            client.setText(R.string.async);
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
                    runner = true;
                    async = false;
                    Popup.error(this, getString(R.string.should_never_happen), 701, null);
//                    return;
            }
            client.setChecked(async);
            client.setEnabled(!runner);

            commandLayout.addView(client);


            final TextView tgtLabel = new TextView(this);
            tgtLabel.setText("Targeting");
            commandLayout.addView(tgtLabel, layoutParams);
            final EditText tgt = new EditText(this);
            tgt.setText(runner ? getString(R.string.not_applicable) : mJSONCommand.getString("tgt"));
            tgt.setEnabled(!runner);
            commandLayout.addView(tgt);

            final TextView funLabel = new TextView(this);
            funLabel.setText("Function");
            commandLayout.addView(funLabel, layoutParams);
            final EditText fun = new EditText(this);
            fun.setText(mJSONCommand.getString("fun"));
            commandLayout.addView(fun);

            final JSONArray args = mJSONCommand.optJSONArray(runner ? "args" : "arg");
            final int argLen = (args == null) ? 0 : args.length();
            final EditText[] argViews = new EditText[argLen];
            if (args != null) {
                final TextView argLabel = new TextView(this);
                argLabel.setText("Arguments");
                commandLayout.addView(argLabel, layoutParams);

                for (int i = 0; i < argLen; i++) {
                    argViews[i] = new EditText(this);
                    argViews[i].setText(args.getString(i));
                    commandLayout.addView(argViews[i]);
                }
            }

            final LinearLayout footerButtons = new LinearLayout(this);
            final NamakButton cancelButton = new NamakButton(this);
            cancelButton.setText(R.string.cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask((Activity) v.getContext());
                }
            });
            footerButtons.addView(cancelButton);

            final Divider divider = new Divider(this);
            footerButtons.addView(divider);

            final NamakButton setButton = new NamakButton(this);
            setButton.setText(R.string.set_params);
            setButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final StringBuilder commandString = new StringBuilder(runner ?
                            String.format("{\"client\":\"runner\",\"fun\":\"%s\"",
                                    fun.getText()
                            ) :
                            String.format("{\"client\":\"%s\",\"tgt\":\"%s\",\"fun\":\"%s\"",
                                    client.isChecked() ? "local_async" : "local", tgt.getText(), fun.getText()
                            ));

                    if (argLen > 0) {
                        commandString.append(runner? ",\"args\":[\"" : ",\"arg\":[\"").append(argViews[0].getText()).append("\"");
                        for (int i = 1; i < argLen; i++) {
                            commandString.append(",\"").append(argViews[i].getText()).append("\"");
                        }
                        commandString.append("]");
                    }
                    commandString.append("}");

                    Intent intent = new Intent();
                    intent.putExtra(CommandModificationActivity.COMMAND_JSON, (CharSequence) commandString);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            footerButtons.addView(setButton);
            commandLayout.addView(footerButtons, layoutParams);
        } catch (JSONException error) {
            Popup.error(this, getString(R.string.should_never_happen), 700, error);
        }


        final ScrollView scroll = new ScrollView(this);
//        scroll.setBackgroundColor(android.R.color.transparent);
//        scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.FILL_PARENT));
        scroll.addView(commandLayout);
        setContentView(scroll);
    }

}