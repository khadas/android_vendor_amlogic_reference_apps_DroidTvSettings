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
import android.os.Handler;
import android.os.Message;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.SystemControlManager;

public class FrameRateFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "FrameRateFragment";

    private static final String KEY_ENABLE_FRAME_RATE = "frame_rate_enable";

    private Context mContext;
    private static FrameRateService mService;
    private static final int EVENT_UPDATE = 0;
    private TwoStatePreference mEnableFrameRatePref;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE:
                    updateView();
                    break;
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FrameRateService.FrameRateBinder binder = (FrameRateService.FrameRateBinder) service;
            mService = binder.getService();
            mHandler.sendEmptyMessage(EVENT_UPDATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public static FrameRateFragment newInstance() {
        return new FrameRateFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mContext.bindService(new Intent(mContext, FrameRateService.class),
            mConnection, mContext.BIND_AUTO_CREATE);
    }


    @Override
    public void onStop() {
        super.onStop();
        mContext.unbindService(mConnection);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.framerate, null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_ENABLE_FRAME_RATE)) {
            setFrameRateEnabled((boolean) newValue);
        }
        return true;
    }

    private void updateView() {
        mEnableFrameRatePref = (TwoStatePreference) findPreference(KEY_ENABLE_FRAME_RATE);
        mEnableFrameRatePref.setOnPreferenceChangeListener(this);
        mEnableFrameRatePref.setChecked(getFrameRateEnabled());
    }

    private boolean getFrameRateEnabled() {
        return mService.getFrameRateEnabled();
    }

    private void setFrameRateEnabled(boolean enable) {
        mService.setFrameRateEnabled(enable);

    }
}
