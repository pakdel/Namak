package com.amirpakdel.namak;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final int horizontal_padding, horizontal_padding_large, vertical_padding;

    public DashboardAdapter(final Context context) {
        this.context = context;
        Resources r = context.getResources();
        horizontal_padding = (int) r.getDimension(R.dimen.activity_horizontal_padding);
        horizontal_padding_large = (int) r.getDimension(R.dimen.activity_horizontal_padding_large);
        vertical_padding = (int) r.getDimension(R.dimen.activity_vertical_padding);
    }

    @Override
    public int getGroupCount() {
        // Here
        Log.d("DashboardAdapter", String.format("Size: %d", NamakApplication.getDashboards().size()));
        return NamakApplication.getDashboards().size();
    }

    @Override
    public int getChildrenCount(int i) {
        // Here
        Log.d("DashboardAdapter", String.format("Size of %d (%d): %d", i, NamakApplication.getDashboards().keyAt(i), NamakApplication.getDashboards().valueAt(i).length()));
        return NamakApplication.getDashboards().valueAt(i).length();
    }

    @Override
    public Object getGroup(int i) {
        return NamakApplication.getDashboards().valueAt(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        try {
            return NamakApplication.getDashboards().valueAt(i).getJSONObject(i1);
        } catch (JSONException error) {
            //FIXME this should never happen
//            error.printStackTrace();
            return null;
        }
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i * 1000 + i1;
    }

    @Override
    public boolean hasStableIds() {
        // TODO maybe true
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new TextView(context, null);
//            view.setPadding(90, 40, 50, 40);
            view.setPadding(horizontal_padding_large, vertical_padding, horizontal_padding, vertical_padding);
            ((TextView) view).setTextColor(Color.BLACK);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }
        ((TextView) view).setText(NamakApplication.getDashboardName(i));
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new TextView(context, null);
//            view.setPadding(90, 40, 50, 40);
            view.setPadding(horizontal_padding_large, vertical_padding, horizontal_padding, vertical_padding);
            ((TextView) view).setTextColor(Color.BLACK);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }
        String dashboardItem = null;
        try {
            dashboardItem = ((JSONObject) getChild(i, i1)).getString("title");
        } catch (JSONException e) {
            //FIXME this should never happen
//            error.printStackTrace();
            dashboardItem = "This should never happen!";
        }
        ((TextView) view).setText(dashboardItem);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
