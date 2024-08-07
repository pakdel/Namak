package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashSet;

import Namak.R;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener,
                   NamakApplication.DashboardListener,
                   NamakApplication.SaltMasterListener {

    private ListView mDrawerListView;
    private ExpandableListView mMainView;
    private TextView mHeader;
    private Snackbar mRelogin;
    private Handler mExpirationHandler = new Handler();
    private Runnable mExpirationRunnable = new Runnable() {
        @Override
        public void run() {
            updateSaltMasterStatus(false);
        }
    };


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

        final SharedPreferences prefs = NamakApplication.getPref();
        // It should be also handled properly in the Settings activity
        if (prefs.getStringSet("saltmasters", new HashSet<String>()).size() < 1
                || prefs.getStringSet("dashboards", new HashSet<String>()).size() < 1) {
            Intent intent = new Intent(NamakApplication.getAppContext(), GeneralSettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return;
        }
        updateSaltMasterStatus(true);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onPause() {
        super.onPause();
//        NamakApplication.getPref().unregisterOnSharedPreferenceChangeListener(prefChanged);
        mExpirationHandler.removeCallbacks(mExpirationRunnable);
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

        mDrawerLayout = new DrawerLayout(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                setTitle(NamakApplication.getSaltMaster().getName());
                updateSaltMasterStatus(false);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                setTitle(R.string.app_name);
                updateSaltMasterStatus(false);
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

        mMainView = new ExpandableListView(this);
        mMainView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mMainView.setAdapter(NamakApplication.getDashboardAdapter());
        mMainView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final SaltMaster sm = NamakApplication.getSaltMaster();
                if (sm.getAuthToken() == null) {
                    Popup.error(mainActivity, getString(R.string.not_logged_in), 201, null);
                    return false;
                }
                if (sm.getExpiration() < System.currentTimeMillis()) {
                    Popup.error(mainActivity, getString(R.string.session_expired, new Date(sm.getExpiration())), 202, null);
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
        mSwipeRefreshLayout.addView(mMainView);
        NamakApplication.addDashboardListener(this);
        NamakApplication.addSaltMasterListener(this);

        mDrawerLayout.addView(mSwipeRefreshLayout);
        mDrawerLayout.addView(mDrawerListView);

        setContentView(mDrawerLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem settingsMenuItem = menu.add(R.string.action_settings);
        settingsMenuItem.setIcon(android.R.drawable.ic_menu_preferences);
        settingsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        settingsMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(NamakApplication.getAppContext(), GeneralSettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    private void updateSaltMasterStatus(boolean forceOpenDrawer) {
        mExpirationHandler.removeCallbacks(mExpirationRunnable);
        final SaltMaster sm = NamakApplication.getSaltMaster();
//        final String AuthToken = sm.getAuthToken();
        final long Expiration = sm.getExpiration();

        if (Expiration > System.currentTimeMillis()) {  // sm.getAuthToken() != null
            // mDrawerListView.setEnabled(true) does nothing!
            setTitle(NamakApplication.getSaltMaster().getName());
            if (mHeader != null) {
                mMainView.removeHeaderView(mHeader);
            }
            // mDrawerLayout.closeDrawers();
            if (mRelogin != null) {
                mRelogin.dismiss();
                mRelogin = null;
            }
            mExpirationHandler.postDelayed(mExpirationRunnable, Expiration - System.currentTimeMillis());
            return;
        }

        if (Expiration == 0) {  // sm.getAuthToken() == null
            // It is too much to do all four!
            // mDrawerListView.setEnabled(false) does nothing!
            setTitle(R.string.app_name);
            if (mHeader == null) {
                mHeader = new TextView(this);
                NamakApplication.getDashboardAdapter().setPadding(mHeader);
                mHeader.setTextColor(Color.DKGRAY);
                mHeader.setText(R.string.log_in);
                mMainView.addHeaderView(mHeader);
            }
            // Force opening the left drawer might be too intrusive!
            if (forceOpenDrawer) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                Popup.message(getString(R.string.log_in));
            }
            return;
        }

        // Expiration < System.currentTimeMillis() => AuthToken is not null, but it is expired
        // mDrawerListView.setEnabled(false) does nothing!
        //noinspection ResourceType
        if (mRelogin == null) {
            mRelogin = Snackbar.make(mMainView, getString(R.string.session_expired, new Date(sm.getExpiration())), Snackbar.LENGTH_INDEFINITE)
                    .setAction("ReLogin", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sm.login();
                                }
                            }
                    );
        }
        mRelogin.setActionTextColor(Color.GRAY).show();
//        return;
    }

    @Override
    public void onLoginFinished() {
        updateSaltMasterStatus(false);
    }
}
