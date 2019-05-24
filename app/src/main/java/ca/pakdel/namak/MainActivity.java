package ca.pakdel.namak;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavController navController;

    // TODO Two pane support is not there

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This is actually main entry point of the Application
        // Lets initialize some singletons
        Net.init(getApplicationContext());
        Popup.init(getApplicationContext());
        if (savedInstanceState == null) {
            setupNavigation();
        } // Else, need to wait for onRestoreInstanceState

        SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(this).get(SaltMastersViewModel.class);
        saltMastersViewModel.getSaltMasters().observe(this, this::setSaltMasters);
        saltMastersViewModel.getSelected().observe(this, this::setSelectedSaltMaster);
    }
    private void setSelectedSaltMaster(SaltMaster saltMaster) {
        // TODO observe this Salt Master and react to state changes
        ((TextView) drawerLayout.findViewById(R.id.saltmaster_title)).setText(saltMaster.getName());
        ((TextView) drawerLayout.findViewById(R.id.saltmaster_url)).setText(saltMaster.getBaseUrl());

        ImageView statusView = drawerLayout.findViewById(R.id.saltmaster_status);
        saltMaster.getState().observe(this, states -> {
            if (states.contains(SaltMaster.States.ERROR)) {
                statusView.setImageResource(android.R.drawable.stat_notify_error);
            } else if (states.contains(SaltMaster.States.DISCONNECTED)) {
                statusView.setImageResource(android.R.drawable.presence_offline);
            } else if (states.contains(SaltMaster.States.CONNECTING)) {
                statusView.setImageResource(android.R.drawable.stat_notify_sync);
            } else if (states.contains(SaltMaster.States.CONNECTED)) {
                statusView.setImageResource(android.R.drawable.presence_online);
            } else {
                statusView.setImageResource(android.R.drawable.ic_dialog_alert);
            }
        });

    }


    // This is not supposed to happen often,
    // so we are blindly reloading everything (no optimization)
    private void setSaltMasters(List<SaltMaster> saltMasters) {
        SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(this).get(SaltMastersViewModel.class);
        NavigationView  navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.removeGroup(R.id.menu_masters);
        for (SaltMaster m: saltMasters) {
            MenuItem menuItem = menu.add(R.id.menu_masters, Menu.NONE, Menu.FIRST, m.getName());
            menuItem.setCheckable(true);
            // menuItem.getActionView().setTag(m.id);
            menuItem.setChecked(saltMastersViewModel.isSelected(m));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupNavigation();
    }

    /**
     * Called on first creation and when restoring state.
     */
    private void setupNavigation() {
        // FIXME DrawerLayout over the ActionBar/Toolbar
         Toolbar toolbar = findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
         assert getSupportActionBar() != null;
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationView  navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        // This will send navigation events to onNavigationItemSelected
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen (GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {  // navController.popBackStack() != true
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        drawerLayout.closeDrawers();

        if (item.getGroupId() == R.id.menu_masters) {
            SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(this).get(SaltMastersViewModel.class);
            SaltMaster currentlySelected = saltMastersViewModel.getSelected().getValue();
            if (currentlySelected != null) {
                currentlySelected.getState().removeObservers(this);
            }
            saltMastersViewModel.select(item.getTitle().toString());
            return true;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_settings:
                navController.navigate(R.id.action_dashboard_to_settings);
                break;

            case R.id.nav_share:
            case R.id.nav_about:
                navController.navigate(R.id.aboutScreen);
                break;
        }
        return true;
    }
}
