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
    private static final String KEY_BALANCE_DEBUG = "key_balance_debug";
    private static final String KEY_TREBLEBASS_DEBUG = "key_treblebass_debug";
    private static final String KEY_VIRTUAL_SURROUND_DEBUG = "key_virtual_surround_debug";
    private static final String KEY_AGC_DEBUG = "key_agc_debug";
    private static final String KEY_DBX_TV_DEBUG = "key_dbx_tv_debug";
    private static final String KEY_TRUSURROUND_DEBUG = "key_trusurround_debug";
    private static final String KEY_VIRTUAL_X_DEBUG = "key_virtual_x_debug";
    private static final String KEY_DAP_2_DEBUG = "key_dap_2_debug";
    private static final String KEY_DOLBY_DRC_DEBUG = "key_dolby_drc_debug";
    private static final String KEY_DTS_DRC_DEBUG = "key_dts_drc_debug";
    private static final String KEY_FORCE_DDP_DEBUG = "key_force_ddp_debug";
    private static final String KEY_AUDIO_LATENCY_DEBUG = "key_audio_latency_debug";

    private TwoStatePreference mHpeqDebug;
    private TwoStatePreference mBalanceDebug;
    private TwoStatePreference mTreblebassDebug;
    private TwoStatePreference mVirtualSDebug;
    private TwoStatePreference mAgcDebug;
    private TwoStatePreference mDbxTvDebug;
    private TwoStatePreference mTruSurroundDebug;
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

        mBalanceDebug = (TwoStatePreference) findPreference(KEY_BALANCE_DEBUG);
        mBalanceDebug.setOnPreferenceChangeListener(this);

        mTreblebassDebug = (TwoStatePreference) findPreference(KEY_TREBLEBASS_DEBUG);
        mTreblebassDebug.setOnPreferenceChangeListener(this);

        mVirtualSDebug = (TwoStatePreference) findPreference(KEY_VIRTUAL_SURROUND_DEBUG);
        mVirtualSDebug.setOnPreferenceChangeListener(this);

        mAgcDebug = (TwoStatePreference) findPreference(KEY_AGC_DEBUG);
        mAgcDebug.setOnPreferenceChangeListener(this);
        mAgcDebug.setVisible(false);

        mDbxTvDebug = (TwoStatePreference) findPreference(KEY_DBX_TV_DEBUG);
        mDbxTvDebug.setOnPreferenceChangeListener(this);

        mTruSurroundDebug = (TwoStatePreference) findPreference(KEY_TRUSURROUND_DEBUG);
        mTruSurroundDebug.setOnPreferenceChangeListener(this);
        mTruSurroundDebug.setVisible(false);

        mVirtualXDebug = (TwoStatePreference) findPreference(KEY_VIRTUAL_X_DEBUG);
        mVirtualXDebug.setOnPreferenceChangeListener(this);

        mDap2Debug = (TwoStatePreference) findPreference(KEY_DAP_2_DEBUG);
        mDap2Debug.setOnPreferenceChangeListener(this);

        if (mOutputModeManager.isAudioSupportMs12System()) {
            mHpeqDebug.setVisible(false);
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

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_HPEQ_UI);
        mHpeqDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_BALANCE_UI);
        mBalanceDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_TREBLEBASS_UI);
        mTreblebassDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_VIRTUAL_SURROUND_UI);
        mVirtualSDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_AGC_UI);
        mAgcDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_DBX_TV_UI);
        mDbxTvDebug.setChecked(enable);

        enable = mAudioEffectManager.isAudioEffectOn(AudioEffectManager.DEBUG_TRUSURROUND_UI);
        mTruSurroundDebug.setChecked(enable);

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
            case KEY_AGC_DEBUG:
                isChecked = mAgcDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_AGC_UI, isChecked);
                break;
            case KEY_DBX_TV_DEBUG:
                isChecked = mDbxTvDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_DBX_TV_UI, isChecked);
                break;
            case KEY_TRUSURROUND_DEBUG:
                isChecked = mTruSurroundDebug.isChecked();
                mAudioEffectManager.setAudioEffectOn(AudioEffectManager.DEBUG_TRUSURROUND_UI, isChecked);
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
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
