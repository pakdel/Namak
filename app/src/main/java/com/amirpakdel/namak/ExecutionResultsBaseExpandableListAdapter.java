package com.amirpakdel.namak;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Namak.R;

class ExecutionResultsBaseExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context mContext;
    private List<String> mMinionNames;
    private List<Object> mReturedResult;

    public ExecutionResultsBaseExpandableListAdapter(Context context) {
        mContext = context;
    }

    public void setData(JSONObject dataStructure) {
        if (dataStructure.length() == 0) {
            mMinionNames = new ArrayList<>(1);
            mReturedResult = new ArrayList<>(1);
            mMinionNames.add(NamakApplication.getAppContext().getString(R.string.no_result));
            mReturedResult.add(NamakApplication.getAppContext().getString(R.string.empty_response));
            notifyDataSetChanged();
            return;
        }
        mMinionNames = new ArrayList<>(dataStructure.length());
        mReturedResult = new ArrayList<>(dataStructure.length());

        Iterator<?> keys = dataStructure.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            mMinionNames.add(key);
            try {
                mReturedResult.add(dataStructure.get(key));
            } catch (JSONException e) {
                mReturedResult.add("Failed to get " + key);
            }
        }
        notifyDataSetChanged();
    }

    public String getData() {
        if (mMinionNames == null) {
            return null;
        }
        JSONObject data = new JSONObject();
        for (int i = 0; i < mMinionNames.size(); i++) {
            try {
                data.put(mMinionNames.get(i), mReturedResult.get(i));
            } catch (JSONException e) {
                // If the value is non-finite number or if the key is null.
                e.printStackTrace();
            }
        }
        return data.toString();
    }

    @Override
    public int getGroupCount() {
        return mMinionNames == null ? 0 : mMinionNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mMinionNames == null ? null : mMinionNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
//        assert childPosition == 0;
        return mReturedResult.get(groupPosition);
    }

    private String getChildString(int groupPosition, int childPosition) {
//        assert childPosition == 0;
        String childString;
        try {
            childString = ((JSONObject) mReturedResult.get(groupPosition)).toString(2);
        } catch (Exception e1) {
            try {
                childString = ((JSONArray) mReturedResult.get(groupPosition)).toString(2);
            } catch (Exception e2) {
                childString = mReturedResult.get(groupPosition).toString();
            }
        }
        return childString;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(mContext);
            convertView.setPadding(90, 20, 10, 20);
//            ((TextView) convertView).setTextSize(18);
            ((TextView) convertView).setTypeface(null, Typeface.BOLD);
        }
        ((TextView) convertView).setText(mMinionNames.get(groupPosition));
        if (getGroupCount() == 1) {
            ((ExpandableListView) parent).expandGroup(0);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        assert childPosition == 0;
        if (convertView == null) {
            convertView = new TextView(mContext);
            convertView.setPadding(30, 5, 10, 20);
//            ((TextView) convertView).setTextSize(18);
            ((TextView) convertView).setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        }
        ((TextView) convertView).setText(getChildString(groupPosition, childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
