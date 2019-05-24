package ca.pakdel.namak;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SaltMastersViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPref;
    private MutableLiveData<List<SaltMaster>> saltMasters;
    private MutableLiveData<SaltMaster> selected;

    public SaltMastersViewModel(@NonNull Application application) {
        super(application);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // selected is used in reloadMasters(), so let's initialize it first
        assert selected == null;
        selected = new MutableLiveData<>();
        assert selected.getValue() == null;

        assert saltMasters == null;
        saltMasters = new MutableLiveData<>();
        reloadMasters();
    }


    // This is not supposed to happen often,
    // so we are blindly reloading everything (no optimization)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith(SaltMaster.PREFERENCE_KEY_PREFIX)) {
            reloadMasters();
        }
    }

    public LiveData<List<SaltMaster>> getSaltMasters() {
        // FIXME verify this will never happen
/*
        if (saltMasters == null) {
            saltMasters = new MutableLiveData<>();
            saltMasters.setValue(new ArrayList<>());
            reloadMasters();
        }
*/
        assert saltMasters != null;
        return saltMasters;
    }

    // sharedPref.getStringSet(...) is not null
    @SuppressWarnings("ConstantConditions")
    private void reloadMasters() {
        // Does not need to be Async. It is quite fast.
        if (! sharedPref.contains(SaltMaster.PREFERENCE_KEY)) {
            saltMasters.setValue(new ArrayList<>());
            return;
        }
        List<SaltMaster> tempSaltMasters =
                sharedPref.getStringSet(SaltMaster.PREFERENCE_KEY, new HashSet<>())
                        .parallelStream()
                        .map(saltmasterID -> new SaltMaster(sharedPref, saltmasterID))
                        .collect(Collectors.toList());
        saltMasters.setValue(tempSaltMasters);

        // Re-select what was already selected
        // It will also login, if the token already expired
        if (selected.getValue() != null) select(selected.getValue().getId());
    }

    public SaltMaster find(String id) {
        assert saltMasters != null;
        assert saltMasters.getValue() != null;
        for (SaltMaster saltMaster: saltMasters.getValue()) {
            if (saltMaster.getId().equals(id)) {
                return saltMaster;
            }
        }
        return null;
    }

    void select(String criterion){
        assert saltMasters != null;
        assert saltMasters.getValue() != null;
        for (SaltMaster saltMaster: saltMasters.getValue()) {
            if (saltMaster.getId().equals(criterion) || saltMaster.getName().equals(criterion)) {
                saltMaster.login();
                selected.setValue(saltMaster);
                return;
            }
        }
        selected.setValue(null);
    }

    LiveData<SaltMaster> getSelected() { return selected; }
    void execute(Command command, MutableLiveData<String> result) {
        selected.getValue().execute(command, result);
    }

    boolean isSelected(SaltMaster saltMaster) { return selected.getValue() != null && selected.getValue().getId().equals(saltMaster.getId()); }
}
