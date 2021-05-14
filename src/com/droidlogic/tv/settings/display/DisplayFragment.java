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

package com.droidlogic.tv.settings.display;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.text.TextUtils;
import android.util.Log;


import android.os.SystemProperties;
import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.R;

import com.droidlogic.app.SystemControlManager;

public class DisplayFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DisplayFragment";

    private static final String KEY_POSITION        = "position";
    private static final String KEY_OUTPUTMODE      = "outputmode";
    private static final String KEY_HDR             = "hdr";
    private static final String KEY_SDR             = "sdr";
    private static final String KEY_DOLBY_VISION    = "dolby_vision";
    private static final String KEY_MEMC            = "memc";
    private static final String PROP_MEMC           = "persist.vendor.sys.memc";
    private static final int MEMC_OFF               = 0;
    private static final int MEMC_ON                = 1;

    private boolean mTvUiMode;
    private ListPreference mMEMCPref;
    private SystemControlManager mSystemControlManager;
    private static int memcStatus = 0;
    private static boolean isT3Device = false;

    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display, null);
        mTvUiMode = DroidUtils.hasTvUiMode();

        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
            && (SystemProperties.getBoolean("tv.soc.as.mbox", false) == false);
        final Preference outputmodePref = findPreference(KEY_OUTPUTMODE);
        outputmodePref.setVisible(SettingsConstant.needScreenResolutionFeture(getContext()) && !tvFlag);

        final Preference screenPositionPref = findPreference(KEY_POSITION);
        screenPositionPref.setVisible(!tvFlag);

        final Preference sdrPref = findPreference(KEY_SDR);
        sdrPref.setVisible(false);

        final Preference hdrPref = findPreference(KEY_HDR);
        hdrPref.setVisible(false);

        final Preference dvPref =(Preference) findPreference(KEY_DOLBY_VISION);
        dvPref.setVisible((SystemProperties.getBoolean("vendor.system.support.dolbyvision", false) == true)
            && tvFlag);

        mSystemControlManager = SystemControlManager.getInstance();

        if (SystemProperties.getBoolean(PROP_MEMC, false) == true) {
                memcStatus = MEMC_ON;
        }else {
                memcStatus = MEMC_OFF;
        }
        isT3Device = mSystemControlManager.hasMemcFunc();
        mMEMCPref = (ListPreference) findPreference(KEY_MEMC);
        mMEMCPref.setValueIndex(memcStatus);
        mMEMCPref.setVisible(isT3Device);
        mMEMCPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_MEMC)) {
            memcStatus = Integer.parseInt((String)newValue);
            if (mSystemControlManager.hasMemcFunc()) {
                if (memcStatus == MEMC_ON) {
                    mSystemControlManager.memcContrl(true);
                }else if (memcStatus == MEMC_OFF) {
                    mSystemControlManager.memcContrl(false);
                }
            }
        }
        return true;
    }
}
