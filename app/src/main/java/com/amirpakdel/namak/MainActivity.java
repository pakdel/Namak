package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NamakApplication.DashboardListener {

    private ListView mDrawerListView;

    private void setSaltMasterNames() {
        mDrawerListView.setAdapter(new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                NamakApplication.getSaltmasterNames()
        ));
        int position = NamakApplication.getSaltMasterIndex();
        if (position >= 0) {
            mDrawerListView.setSelection(position);
            mDrawerListView.setItemChecked(position, true);
        }
    }
    // Caution: The preference manager does not currently store a strong reference to the listener.
    // You must store a strong reference to the listener, or it will be susceptible to garbage collection.
    // We recommend you keep a reference to the listener in the instance data of an object that will exist as long as you need the listener.
    @SuppressWarnings("FieldCanBeLocal")
//    private static SharedPreferences.OnSharedPreferenceChangeListener prefChanged;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.startsWith("saltmasters") || (key.startsWith("saltmaster_") && key.endsWith("_name"))) {
                NamakApplication.loadSaltmasters();
                setSaltMasterNames();
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        NamakApplication.getPref().registerOnSharedPreferenceChangeListener(prefChanged);
        setSaltMasterNames();
    }

    @Override
    public void onPause() {
        super.onPause();
//        NamakApplication.getPref().unregisterOnSharedPreferenceChangeListener(prefChanged);
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Activity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mainActivity = this;
        setTitle(R.string.app_name);

        mDrawerLayout = new DrawerLayout(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                setTitle(NamakApplication.getSaltMaster().getName());
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(R.string.app_name);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerListView = new ListView(this);
        mDrawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                NamakApplication.getSaltMaster().setId(NamakApplication.getSaltMasterId(position));
            }
        });

        DrawerLayout.LayoutParams drawerListViewParams = new DrawerLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.navigation_drawer_width),
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        mDrawerListView.setLayoutParams(drawerListViewParams);
        mDrawerListView.setBackgroundColor(Color.WHITE);

        ExpandableListView mainView = new ExpandableListView(this);
        mainView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainView.setAdapter(NamakApplication.getDashboardAdapter());
        mainView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (NamakApplication.getSaltMaster().getAuthToken() == null) {
                    Popup.error(mainActivity, getString(R.string.not_logged_in), 200, null);
                    return false;
                }
                Intent intent = new Intent(mainActivity, CommandExecutionActivity.class);
                intent.putExtra(CommandExecutionActivity.COMMAND_GROUP_POSITION, groupPosition);
                intent.putExtra(CommandExecutionActivity.COMMAND_CHILD_POSITION, childPosition);

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(intent);
                return true;
            }

        });

        mSwipeRefreshLayout = new SwipeRefreshLayout(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(true);
//        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.addView(mainView);
        NamakApplication.addDashboardListener(this);

        mDrawerLayout.addView(mSwipeRefreshLayout);
        mDrawerLayout.addView(mDrawerListView);

        setContentView(mDrawerLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
        setTitle(NamakApplication.getSaltMaster().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem settingsMenuItem = menu.add(R.string.action_settings);
        settingsMenuItem.setIcon(android.R.drawable.ic_menu_preferences);
        settingsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        settingsMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(NamakApplication.getAppContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        NamakApplication.reloadDashboards();
    }

    @Override
    public void onDashboardLoadFinished() {
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
