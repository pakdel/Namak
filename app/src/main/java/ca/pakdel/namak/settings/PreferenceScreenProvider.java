package ca.pakdel.namak.settings;

import android.content.Context;
import androidx.preference.PreferenceScreen;

public interface PreferenceScreenProvider {
    void updatePreferenceFragment(Context context, PreferenceScreen screen);
}
