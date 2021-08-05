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

package com.android.tv.settings.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.SoftApConfiguration;
import androidx.annotation.VisibleForTesting;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.util.Log;

import com.android.tv.settings.R;

public class WifiTetherApBandPreferenceController extends WifiTetherBasePreferenceController {

    private static final String TAG = "WifiTetherApBandPref";
    private static final String PREF_KEY = "wifi_tether_network_ap_band";

    private String[] mBandEntries;
    private String[] mBandSummaries;
    private int mBandIndex;
    private boolean isDualMode;

    public WifiTetherApBandPreferenceController(Context context,
            OnTetherConfigUpdateListener listener) {
        super(context, listener);
        isDualMode = false;//mWifiManager.isDualModeSupported();
        updatePreferenceEntries();
    }

    @Override
    public void updateDisplay() {
        final SoftApConfiguration config = mWifiManager.getSoftApConfiguration();
        if (config == null) {
            mBandIndex = SoftApConfiguration.BAND_2GHZ;
            Log.d(TAG, "Updating band index to 0 because no config");
        } else if (is5GhzBandSupported()) {
            mBandIndex = validateSelection(config.getBand());
            Log.d(TAG, "Updating band index to " + mBandIndex);
        } else {
            mWifiManager.setSoftApConfiguration(
                    new SoftApConfiguration.Builder(config).setBand(SoftApConfiguration.BAND_2GHZ)
                        .build());
            mBandIndex = SoftApConfiguration.BAND_2GHZ;
            Log.d(TAG, "5Ghz not supported, updating band index to " + mBandIndex);
        }
        ListPreference preference =
                (ListPreference) mPreference;
        preference.setEntries(mBandSummaries);
        preference.setEntryValues(mBandEntries);

        if (!is5GhzBandSupported()) {
            preference.setEnabled(false);
            preference.setSummary(R.string.wifi_ap_choose_2G);
        } else {
            preference.setValue(Integer.toString(config.getBand()));
            preference.setSummary(getConfigSummary());
        }
    }

    String getConfigSummary() {
        if (mBandIndex == SoftApConfiguration.BAND_ANY) {
           return mContext.getString(R.string.wifi_ap_prefer_5G);
        }
        return mBandSummaries[mBandIndex];
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mBandIndex = validateSelection(Integer.parseInt((String) newValue));
        Log.d(TAG, "Band preference changed, updating band index to " + mBandIndex);
        preference.setSummary(getConfigSummary());
        mListener.onTetherConfigUpdated();
        return true;
    }

    private int validateSelection(int band) {
        // Reset the band to 2.4 GHz if we get a weird config back to avoid a crash.
        final boolean isDualMode = false;//mWifiManager.isDualModeSupported();

        // unsupported states:
        // 1: no dual mode means we can't have AP_BAND_ANY - default to 5GHZ
        // 2: no 5 GHZ support means we can't have AP_BAND_5GHZ - default to 2GHZ
        // 3: With Dual mode support we can't have AP_BAND_5GHZ - default to ANY
        if (!isDualMode && SoftApConfiguration.BAND_ANY == band) {
            return SoftApConfiguration.BAND_5GHZ;
        } else if (!is5GhzBandSupported() && SoftApConfiguration.BAND_5GHZ == band) {
            return SoftApConfiguration.BAND_2GHZ;
        } else if (isDualMode && SoftApConfiguration.BAND_5GHZ == band) {
            return SoftApConfiguration.BAND_ANY;
        }

        return band;
    }

    @VisibleForTesting
    void updatePreferenceEntries() {
        Resources res = mContext.getResources();
        int entriesRes = R.array.wifi_ap_band_config_full;
        int summariesRes = R.array.wifi_ap_band_summary_full;
        // change the list options if this is a dual mode device
        if (isDualMode) {
            entriesRes = R.array.wifi_ap_band_dual_mode;
            summariesRes = R.array.wifi_ap_band_dual_mode_summary;
        }
        mBandEntries = res.getStringArray(entriesRes);
        mBandSummaries = res.getStringArray(summariesRes);
    }

    private boolean is5GhzBandSupported() {
        final String countryCode = mWifiManager.getCountryCode();
        if (!mWifiManager.is5GHzBandSupported() || countryCode == null) {
            return false;
        }
        return true;
    }

    public int getBandIndex() {
        return mBandIndex;
    }
}
