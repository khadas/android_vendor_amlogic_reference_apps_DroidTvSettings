/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License.
 */

package com.droidlogic.tv.settings.wifi;

import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
public class WifiTetherAutoOffPreferenceController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "WifiTetherAutoOffPreferenceController";
    private SwitchPreference mSwitchPref;
    private String mPrefKey;
    public WifiTetherAutoOffPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mPrefKey = preferenceKey;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
     @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Log.d(TAG,"displayPreference");
        mSwitchPref = (SwitchPreference)screen.findPreference(mPrefKey);
    }
    @Override
    public void updateState(Preference preference) {
        final boolean settingsOn = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.SOFT_AP_TIMEOUT_ENABLED, 1) != 0;
        mSwitchPref = (SwitchPreference)preference;
        ((SwitchPreference) preference).setChecked(settingsOn);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mSwitchPref = (SwitchPreference)preference;
        final boolean settingsOn = (Boolean) newValue;
        Log.d(TAG,"onPreferenceChange"+preference.getKey()+"new:"+settingsOn);
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.SOFT_AP_TIMEOUT_ENABLED, settingsOn ? 1 : 0);
        return true;
    }

    public void setEnabled(boolean enable) {
        Log.d(TAG,"setEnabled"+enable);
        if (mSwitchPref != null) {
            mSwitchPref.setEnabled(enable);
        }
    }
}
