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

package com.droidlogic.tv.settings.soundeffect;

import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;
import android.util.Log;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.tvoption.SoundParameterSettingManager;
import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.DroidLogicUtils;

public class DebugAudioUIFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DebugAudioUIFragment";

    private static final String KEY_HPEQ_DEBUG = "key_hpeq_debug";
    private static final String KEY_HPEQ_BAND_NUM_DEBUG = "key_hpeq_band_num_debug";
    private static final String KEY_BALANCE_DEBUG = "key_balance_debug";
    private static final String KEY_TREBLEBASS_DEBUG = "key_treblebass_debug";
    private static final String KEY_VIRTUAL_SURROUND_DEBUG = "key_virtual_surround_debug";
    private static final String KEY_DPE_DEBUG = "key_dpe_debug";
    private static final String KEY_VIRTUAL_X_DEBUG = "key_virtual_x_debug";
    private static final String KEY_DAP_2_DEBUG = "key_dap_2_debug";
    private static final String KEY_DOLBY_DRC_DEBUG = "key_dolby_drc_debug";
    private static final String KEY_DTS_DRC_DEBUG = "key_dts_drc_debug";
    private static final String KEY_FORCE_DDP_DEBUG = "key_force_ddp_debug";
    private static final String KEY_AUDIO_LATENCY_DEBUG = "key_audio_latency_debug";

    private TwoStatePreference mHpeqDebug;
    private ListPreference mHpeqBandNumDebug;
    private TwoStatePreference mBalanceDebug;
    private TwoStatePreference mTreblebassDebug;
    private TwoStatePreference mVirtualSDebug;
    private TwoStatePreference mDpeDebug;
    private TwoStatePreference mVirtualXDebug;
    private TwoStatePreference mDap2Debug;
    private TwoStatePreference mDolbyDrcDebug;
    private TwoStatePreference mDtsDrcDebug;
    private TwoStatePreference mForceDDPDebug;
    private TwoStatePreference mAudioLatencyDebug;

    private AudioEffectManager mAudioEffectManager;
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private OutputModeManager mOutputModeManager;

    public static DebugAudioUIFragment newInstance() {
        return new DebugAudioUIFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        if (mAudioEffectManager == null) {
            mAudioEffectManager = AudioEffectManager.getInstance(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateDetail();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d(TAG, "onCreatePreferences");

        if (mSoundParameterSettingManager == null)
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());

        mOutputModeManager = OutputModeManager.getInstance(getActivity());

        setPreferencesFromResource(R.xml.audio_debug, null);

        mHpeqDebug = (TwoStatePreference) findPreference(KEY_HPEQ_DEBUG);
        mHpeqDebug.setOnPreferenceChangeListener(this);

        mHpeqBandNumDebug = (ListPreference) findPreference(KEY_HPEQ_BAND_NUM_DEBUG);
        mHpeqBandNumDebug.setOnPreferenceChangeListener(this);
        if (mHpeqDebug.isChecked() == false) {
            mHpeqBandNumDebug.setVisible(false);
        }
        mBalanceDebug = (TwoStatePreference) findPreference(KEY_BALANCE_DEBUG);
        mBalanceDebug.setOnPreferenceChangeListener(this);

        mTreblebassDebug = (TwoStatePreference) findPreference(KEY_TREBLEBASS_DEBUG);
        mTreblebassDebug.setOnPreferenceChangeListener(this);

        mVirtualSDebug = (TwoStatePreference) findPreference(KEY_VIRTUAL_SURROUND_DEBUG);
        mVirtualSDebug.setOnPreferenceChangeListener(this);
        mDpeDebug = (TwoStatePreference) findPreference(KEY_DPE_DEBUG);
        mDpeDebug.setOnPreferenceChangeListener(this);

        mVirtualXDebug = (TwoStatePreference) findPreference(KEY_VIRTUAL_X_DEBUG);
        mVirtualXDebug.setOnPreferenceChangeListener(this);

        mDap2Debug = (TwoStatePreference) findPreference(KEY_DAP_2_DEBUG);
        mDap2Debug.setOnPreferenceChangeListener(this);

        if (mOutputModeManager.isAudioSupportMs12System()) {
            mHpeqDebug.setVisible(false);
            mHpeqBandNumDebug.setVisible(false);
        } else {
            mDap2Debug.setVisible(false);
        }

        mDolbyDrcDebug = (TwoStatePreference) findPreference(KEY_DOLBY_DRC_DEBUG);
        mDolbyDrcDebug.setOnPreferenceChangeListener(this);

        mDtsDrcDebug = (TwoStatePreference) findPreference(KEY_DTS_DRC_DEBUG);
        mDtsDrcDebug.setOnPreferenceChangeListener(this);

        mForceDDPDebug = (TwoStatePreference) findPreference(KEY_FORCE_DDP_DEBUG);
        mForceDDPDebug.setOnPreferenceChangeListener(this);

        mAudioLatencyDebug = (TwoStatePreference) findPreference(KEY_AUDIO_LATENCY_DEBUG);
        mAudioLatencyDebug.setOnPreferenceChangeListener(this);

        if (!DroidLogicUtils.isTv()) {
            mAudioLatencyDebug.setVisible(false);
        }
    }

    private String getShowString(int resid) {
        return getActivity().getResources().getString(resid);
    }

    private void updateDetail() {
        boolean enable = false;
        int value = 0;

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_HPEQ_UI);
        mHpeqDebug.setChecked(enable);
        mHpeqBandNumDebug.setVisible(enable);

        value = mAudioEffectManager.getHpeqBandNum(AudioEffectManager.DEBUG_HPEQ_BAND_NUM_UI);
        String hpeqBandIndex = Integer.toString(value);
        mHpeqBandNumDebug.setValueIndex(mHpeqBandNumDebug.findIndexOfValue(hpeqBandIndex));

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_BALANCE_UI);
        mBalanceDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_TREBLEBASS_UI);
        mTreblebassDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_SURROUND_UI);
        mVirtualSDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DPE_UI);
        mDpeDebug.setChecked(enable);
        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_X_UI);
        mVirtualXDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DAP_2_UI);
        mDap2Debug.setChecked(enable);

        enable = mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_DOLBY_DRC_UI);
        mDolbyDrcDebug.setChecked(enable);

        enable = mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_DTS_DRC_UI);
        mDtsDrcDebug.setChecked(enable);

        enable = mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_FORCE_DDP_UI);
        mForceDDPDebug.setChecked(enable);

        enable = mSoundParameterSettingManager.isDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI);
        mAudioLatencyDebug.setChecked(enable);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean isChecked;
        switch (preference.getKey()) {
            case KEY_HPEQ_DEBUG:
                isChecked = mHpeqDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_HPEQ_UI, isChecked);
                mHpeqBandNumDebug.setVisible(isChecked);
                break;
            case KEY_BALANCE_DEBUG:
                isChecked = mBalanceDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_BALANCE_UI, isChecked);
                break;
            case KEY_TREBLEBASS_DEBUG:
                isChecked = mTreblebassDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_TREBLEBASS_UI, isChecked);
                break;
            case KEY_VIRTUAL_SURROUND_DEBUG:
                isChecked = mVirtualSDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_SURROUND_UI, isChecked);
                break;
            case KEY_DPE_DEBUG:
                isChecked = mDpeDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_DPE_UI, isChecked);
                break;
            case KEY_VIRTUAL_X_DEBUG:
                isChecked = mVirtualXDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_X_UI, isChecked);
                break;
            case KEY_DAP_2_DEBUG:
                isChecked = mDap2Debug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_DAP_2_UI, isChecked);
                break;
            case KEY_DOLBY_DRC_DEBUG:
                isChecked = mDolbyDrcDebug.isChecked();
                mSoundParameterSettingManager.setDebugAudioOn(SoundParameterSettingManager.DEBUG_DOLBY_DRC_UI, isChecked);
                break;
            case KEY_DTS_DRC_DEBUG:
                isChecked = mDtsDrcDebug.isChecked();
                mSoundParameterSettingManager.setDebugAudioOn(SoundParameterSettingManager.DEBUG_DTS_DRC_UI, isChecked);
                break;
            case KEY_FORCE_DDP_DEBUG:
                isChecked = mForceDDPDebug.isChecked();
                mSoundParameterSettingManager.setDebugAudioOn(SoundParameterSettingManager.DEBUG_FORCE_DDP_UI, isChecked);
                break;
            case KEY_AUDIO_LATENCY_DEBUG:
                isChecked = mAudioLatencyDebug.isChecked();
                mSoundParameterSettingManager.setDebugAudioOn(SoundParameterSettingManager.DEBUG_AUDIO_LATENCY_UI, isChecked);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case KEY_HPEQ_BAND_NUM_DEBUG:
                final int selection = Integer.parseInt((String)newValue);
                mAudioEffectManager.setHpeqBandNum(AudioEffectManager.DEBUG_HPEQ_BAND_NUM_UI, selection);
                break;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
