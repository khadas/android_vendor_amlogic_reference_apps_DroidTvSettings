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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.SystemControlManager;

public class AiPqFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AiPqFragment";

    private static final String KEY_ENABLE_AIPQ = "ai_pq_enable";
    private static final String KEY_ENABLE_AIPQ_INFO = "ai_pq_info_enable";
    private static final String SYSFS_DEBUG_VDETECT = "/sys/module/decoder_common/parameters/debug_vdetect";
    private static final String SYSFS_ADD_VDETECT = "/sys/class/vdetect/tv_add_vdetect";
    private Context mContext;
    private SystemControlManager mSystemControlManager;
    private AiPqService mService;
    private TwoStatePreference enableAipqPref;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        mContext.bindService(new Intent(mContext, AiPqService.class), mConnection, mContext.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mContext.unbindService(mConnection);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.aipq, null);
        mSystemControlManager = SystemControlManager.getInstance();
        enableAipqPref = (TwoStatePreference) findPreference(KEY_ENABLE_AIPQ);
        enableAipqPref.setOnPreferenceChangeListener(this);
        enableAipqPref.setChecked(getAipqEnabled());
        
        enableAipqInfoPref = (TwoStatePreference) findPreference(KEY_ENABLE_AIPQ_INFO);
        enableAipqInfoPref.setOnPreferenceChangeListener(this);
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
                if (mService != null) mService.disableAipq();
            }
            setAipqEnabled((boolean) newValue);
        } else if (TextUtils.equals(preference.getKey(), KEY_ENABLE_AIPQ_INFO)) {
            if ((boolean) newValue) {
                if (mService != null) mService.enableAipq();
            } else {
                if (mService != null) mService.disableAipq();
            }
        }
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AiPqService.AiPqBinder binder = (AiPqService.AiPqBinder) service;
            mService = binder.getService();
            Log.d("V", "bind success0");
            if (!mService.isShowing()) {
                enableAipqInfoPref.setChecked(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private boolean getAipqEnabled() {
        return mSystemControlManager.getAipqEnable();
    }

    private void setAipqEnabled(boolean enable) {
        mSystemControlManager.setAipqEnable(enable);
    }
}
