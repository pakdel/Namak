package com.amirpakdel.namak;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardListAdapter extends BaseAdapter {

    private final Context context;
    private List<String> dashboardList = new ArrayList<>(20);

    public DashboardListAdapter(final Context context) {
//		super(context, android.R.id.text1, new ArrayList<String>(20));
        this.context = context;
    }

    public void add(String dashboardItem) {
        dashboardList.add(dashboardItem);
        notifyDataSetChanged();
    }

    public void clear() {
        dashboardList.clear();
    }

    @Override
    public int getCount() {
        return dashboardList.size();
    }

    @Override
    public String getItem(int position) {
        return dashboardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(context, null);
            convertView.setPadding(90, 40, 50, 40);
            ((TextView) convertView).setTextColor(Color.BLACK);
            ((TextView) convertView).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }
        String dashboardItem = getItem(position);
        ((TextView) convertView).setText(dashboardItem);
        return convertView;
    }
}
