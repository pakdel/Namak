package com.amirpakdel.namak;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private final float /*small_text,*/ large_text;
    private final int background_color;

    public DashboardAdapter(final Context context) {
        this.context = context;
        Resources r = context.getResources();
        horizontal_padding = (int) r.getDimension(R.dimen.activity_horizontal_padding);
        horizontal_padding_large = (int) r.getDimension(R.dimen.activity_horizontal_padding_large);
        vertical_padding = (int) r.getDimension(R.dimen.activity_vertical_padding);

        float normal_text = new TextView(context).getTextSize();
//        small_text = normal_text / 1.1f;
        large_text = normal_text * 1.5f;
        background_color = r.getColor(R.color.LightBlue);
    }

    @Override
    public int getGroupCount() {
        // We can cache it, and then update it when notifyDataSetChanged() is called;
        // but I think it would be an overkill!
        return NamakApplication.getDashboards().size();
    }

    @Override
    public int getChildrenCount(int i) {
        // Same as getGroupCount():
        // We can cache it, and then update it when notifyDataSetChanged() is called;
        // but I think it would be an overkill!
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
            Popup.error(NamakApplication.getForegroundActivity(), context.getString(R.string.should_never_happen), 301, error);
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
            view.setPadding(horizontal_padding_large, vertical_padding, horizontal_padding, vertical_padding);
            ((TextView) view).setTextColor(Color.BLACK);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, large_text);
            ((TextView) view).setTypeface(Typeface.DEFAULT_BOLD);
            view.setBackgroundColor(background_color);
        }
        ((TextView) view).setText(NamakApplication.getDashboardName(i));
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new TextView(context, null);
            view.setPadding(horizontal_padding, vertical_padding, horizontal_padding, vertical_padding);
            ((TextView) view).setTextColor(Color.BLACK);
        }
        String dashboardItem;
        try {
            dashboardItem = ((JSONObject) getChild(i, i1)).getString("title");
        } catch (JSONException error) {
            dashboardItem = context.getString(R.string.should_never_happen);
            Popup.error(null, dashboardItem, 302, error);
        }
        ((TextView) view).setText(dashboardItem);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
