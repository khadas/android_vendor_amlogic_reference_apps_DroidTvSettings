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

package com.droidlogic.tv.settings;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.droidlogic.app.SystemControlManager;

public class AiPqFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AiPqFragment";

    private static final String KEY_ENABLE_AIPQ = "ai_pq_enable";
    public static final String KEY_ENABLE_AIPQ_INFO = "ai_pq_info_enable";
    private static final String KEY_ENABLE_AISR = "ai_sr_enable";
    private static final String SYSFS_DEBUG_VDETECT = "/sys/module/decoder_common/parameters/debug_vdetect";
    private static final String SYSFS_ADD_VDETECT = "/sys/class/vdetect/tv_add_vdetect";

    private static final String PROP_AIPQ_ENABLE = "persist.vendor.sys.aipq.info";
    private static final String SAVE_AIPQ = "AIPQ";
    private static final int AIPQ_ENABLE = 1;
    private static final int AIPQ_DISABLE = 2;

    private Context mContext;
    private SystemControlManager mSystemControlManager;
    private TwoStatePreference enableAipqPref;
    private TwoStatePreference enableAisrPref;
    private TwoStatePreference enableAipqInfoPref;
    public static AiPqFragment newInstance() {
        return new AiPqFragment();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.aipq, null);
        mSystemControlManager = SystemControlManager.getInstance();
        enableAipqPref = (TwoStatePreference) findPreference(KEY_ENABLE_AIPQ);
        enableAipqPref.setOnPreferenceChangeListener(this);
        enableAipqPref.setChecked(getAipqEnabled());

        enableAisrPref = (TwoStatePreference) findPreference(KEY_ENABLE_AISR);
        enableAisrPref.setOnPreferenceChangeListener(this);
        enableAisrPref.setChecked(getAisrEnabled());

        enableAipqInfoPref = (TwoStatePreference) findPreference(KEY_ENABLE_AIPQ_INFO);
        enableAipqInfoPref.setOnPreferenceChangeListener(this);
        Log.i(TAG, "init Aipqinfo: " + getAipqinfo());
        enableAipqInfoPref.setChecked(getAipqinfo());

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AIPQ)) {
            if ((boolean) newValue) {
                enableAipqInfoPref.setEnabled(true);
            } else {
                enableAipqInfoPref.setEnabled(false);
                enableAipqInfoPref.setChecked(false);
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "false");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.System.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_DISABLE);
            }
            setAipqEnabled((boolean) newValue);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AIPQ_INFO)) {
            if ((boolean) newValue) {
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "true");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.System.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_ENABLE);
            } else {
                mSystemControlManager.setProperty(PROP_AIPQ_ENABLE, "false");
                // As a result of SWPL-67409, AiPqService has been migrated
                // into DroidLogic as a viable service using ContentObserver listening.
                Settings.System.putInt(getActivity().getContentResolver(), SAVE_AIPQ, AIPQ_DISABLE);
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AISR)) {
            setAisrEnabled((boolean) newValue);
            Log.d(TAG, "AipqEnabled=" + getAisrEnabled() + ", Nvalue= " + newValue);
        }
        return true;
    }

    private boolean getAipqEnabled() {
        Log.d(TAG, "getAipqEnabled:" + mSystemControlManager.getAipqEnable());
        return mSystemControlManager.getAipqEnable();
    }
    private boolean getAipqinfo() {
        return mSystemControlManager.getPropertyBoolean(PROP_AIPQ_ENABLE, false);
    }

    private boolean getAisrEnabled() {
        return mSystemControlManager.GetAisr();
    }

    private void setAipqEnabled(boolean enable) {
        mSystemControlManager.setAipqEnable(enable);
    }

    private void setAisrEnabled(boolean enable) {
        //mSystemControlManager.setAiSrEnable(enable);
        mSystemControlManager.aisrContrl(enable);
    }
}
