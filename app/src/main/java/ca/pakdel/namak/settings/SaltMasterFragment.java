package ca.pakdel.namak.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import ca.pakdel.namak.*;

public class SaltMasterFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_salt_master, rootKey);

        SaltMastersViewModel saltMastersViewModel = ViewModelProviders.of(getActivity()).get(SaltMastersViewModel.class);
        assert getArguments() != null;
        String saltMasterId = getArguments().getString(SaltMaster.ID);
        SaltMaster saltMaster = saltMastersViewModel.find(saltMasterId);
        assert saltMaster != null;
        // TODO maybe getPreferenceManager().getContext()
        saltMaster.updatePreferenceFragment(getContext(), getPreferenceScreen());
    }
}
