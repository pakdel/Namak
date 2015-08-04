package com.amirpakdel.namak;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SaltMaster.DashboardListener {
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
//                supportInvalidateOptionsMenu();
                setTitle(R.string.title_dashboard);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                supportInvalidateOptionsMenu();
                setTitle(R.string.app_name);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ListView mDrawerListView = new ListView(this);
        // TODO use the following when the rest of the drawer items are implemented
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        // mDrawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                if (position != 0) {
                    Toast.makeText(mainActivity, "Section " + position + " is not implemented yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDrawerListView.setAdapter(new ArrayAdapter<>(
                mainActivity,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new String[]{
                        getString(R.string.title_dashboard),
                        getString(R.string.title_minions),
                        getString(R.string.title_cloud),
                        getString(R.string.title_runners),
                        getString(R.string.title_wheels),
                }));

        DrawerLayout.LayoutParams drawerListViewParams = new DrawerLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.navigation_drawer_width),
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        mDrawerListView.setLayoutParams(drawerListViewParams);
        mDrawerListView.setBackgroundColor(Color.WHITE);

        ListView mainView = new ListView(this);
        mainView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainView.setAdapter(NamakApplication.getDashboardListAdapter());
        mainView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mainActivity, CommandExecutionActivity.class);
                intent.putExtra(CommandExecutionActivity.COMMAND_ITEM_POSITION, position);
                startActivity(intent);
            }

        });

        mSwipeRefreshLayout = new SwipeRefreshLayout(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setEnabled(true);
//        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.addView(mainView);
        NamakApplication.getSaltMaster().addDashboardListener(this);

        mDrawerLayout.addView(mSwipeRefreshLayout);
        mDrawerLayout.addView(mDrawerListView);

        setContentView(mDrawerLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
        // Default, and the only implemented feature, is Dashboard
        setTitle(R.string.title_dashboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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

    @Override
    public void onRefresh() {
        NamakApplication.getSaltMaster().loadDashboard();
    }

    @Override
    public void onDashboardLoadFinished(JSONObject dashboard) {
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
