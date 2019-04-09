
package com.droidlogic.tv.settings.tvoption;

import com.droidlogic.tv.settings.BaseSettingsFragment;
import com.droidlogic.tv.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control HDMI CEC settings.
 */
public class HdmiCecManualWakeUpActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return SettingsFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final HdmiCecManualWakeUpFragment fragment = HdmiCecManualWakeUpFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
