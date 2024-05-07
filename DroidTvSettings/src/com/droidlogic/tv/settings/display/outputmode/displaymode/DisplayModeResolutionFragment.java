/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.display.outputmode.displaymode;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;

import com.droidlogic.app.SystemControlManager;

import static com.droidlogic.tv.settings.util.DroidUtils.logDebug;

public class DisplayModeResolutionFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplayModeResolutionFragment";
    private static final String DISPLAY_MODE_0 = "display_mode_0";
    private static final String DISPLAY_MODE_1 = "display_mode_1";

    private Preference DISPLAY_MODE_0_Pref;
    private Preference DISPLAY_MODE_1_Pref;

    public static DisplayModeResolutionFragment newInstance() {
        return new DisplayModeResolutionFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display_mode_resolution, null);
        DISPLAY_MODE_0_Pref = (Preference) findPreference(DISPLAY_MODE_0);
        DISPLAY_MODE_1_Pref = (Preference) findPreference(DISPLAY_MODE_1);
        updatePreference();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
/*        if (TextUtils.equals(preference.getKey(), KEY_BEST_RESOLUTION)) {

            }
        } else if (TextUtils.equals(preference.getKey(), KEY_BEST_DOLBYVISION)) {

        }*/
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }


    public void updatePreference() {
        final String supportDisplayId = getDisplayModeNameMultiTiming();
        switch (supportDisplayId) {
            case "0/1":
                DISPLAY_MODE_0_Pref.setVisible(true);
                DISPLAY_MODE_1_Pref.setVisible(true);
                break;
            case "0":
                DISPLAY_MODE_0_Pref.setVisible(true);
                DISPLAY_MODE_1_Pref.setVisible(false);
                break;
            case "1":
                DISPLAY_MODE_0_Pref.setVisible(false);
                DISPLAY_MODE_1_Pref.setVisible(true);
                break;
            default:
                DISPLAY_MODE_0_Pref.setVisible(false);
                DISPLAY_MODE_1_Pref.setVisible(false);
                logDebug(TAG, true, "display mode name:" + getDisplayModeNameMultiTiming());
                break;
        }
    }

    public String getDisplayModeNameMultiTiming() {
        SystemControlManager sm = SystemControlManager.getInstance();
        return sm.getProperty("vendor.extended.mode.display_ids");
    }
}
