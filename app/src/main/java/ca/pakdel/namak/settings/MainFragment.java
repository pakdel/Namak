package ca.pakdel.namak.settings;

import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import ca.pakdel.namak.*;

import java.util.List;

public class MainFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_main, rootKey);

        // Context context = getPreferenceManager().getContext();
        // PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
        // assert getView() != null;
        // NavController navController = Navigation.findNavController(getView());

        assert getActivity() != null;
        SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(getActivity()).get(SaltMastersViewModel.class);
        saltMastersViewModel.getSaltMasters().observe(this, this::setSaltMasters);

        DashboardsViewModel dashboardsViewModel = ViewModelProviders.of(getActivity()).get(DashboardsViewModel.class);
        dashboardsViewModel.getDashboards().observe(this, this::setDashboards);
    }


    private void setSaltMasters(List<SaltMaster> saltMasters) {
        PreferenceCategory saltMasterPreferenceCategory =  findPreference("saltmaster_category");
        assert saltMasterPreferenceCategory != null;
        saltMasterPreferenceCategory.removeAll();
        for (SaltMaster saltMaster: saltMasters) {
            // TODO addPreferencesFromResource() ?
            Preference saltMasterPreference = new SaltMasterPreferenceButton(getContext(), saltMaster);
            final Bundle bundle = new Bundle();
            bundle.putString(SaltMaster.ID, saltMaster.getId());
            assert getView() != null;
            saltMasterPreference.setOnPreferenceClickListener(p -> {Navigation.findNavController(getView()).navigate(R.id.action_main_settings_to_salt_master, bundle); return true;});
            saltMasterPreferenceCategory.addPreference(saltMasterPreference);
        }
        assert getContext() != null;
        Preference addSaltMasterPreference = new Preference(getContext());
        addSaltMasterPreference.setTitle("Add Salt Master");
        addSaltMasterPreference.setKey("add_saltmaster");
        addSaltMasterPreference.setIcon(android.R.drawable.ic_input_add);
        saltMasterPreferenceCategory.addPreference(addSaltMasterPreference);
    }

    private void setDashboards(List<Dashboard> dashboards) {
        PreferenceCategory dashboardPreferenceCategory = findPreference("dashboard_category");
        assert dashboardPreferenceCategory != null;
        for (Dashboard dashboard: dashboards) {
            // TODO addPreferencesFromResource() ?
            Preference dashboardPreference = new DashboardPreferenceButton(getContext(), dashboard);
            dashboardPreference.setOnPreferenceClickListener(p -> true);
            dashboardPreferenceCategory.addPreference(dashboardPreference);
        }
    }
}


class PreferenceButton extends Preference {
    PreferenceButton(Context context, String key, String title, String summary) {
        super(context);
        setKey(key);
        setTitle(title);
        setSummary(summary);
    }
}

class SaltMasterPreferenceButton extends PreferenceButton {
    SaltMasterPreferenceButton(Context context, SaltMaster master) {
        super(context, "salt_master_" + master.getId(), master.getName(), master.getBaseUrl());
    }
}
class DashboardPreferenceButton extends PreferenceButton {
    DashboardPreferenceButton(Context context, Dashboard dashboard) {
        super(context, "dashboard_" + dashboard.getId(), dashboard.getName(), dashboard.getUrl());
    }
}