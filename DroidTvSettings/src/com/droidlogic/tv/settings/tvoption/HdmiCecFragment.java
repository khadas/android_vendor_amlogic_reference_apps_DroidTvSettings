/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC HdmiCecFragment
 */

package com.droidlogic.tv.settings.tvoption;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SeekBarPreference;
import androidx.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.AudioConfigManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SoundFragment;

import java.util.*;

import android.os.SystemProperties;
/**
 * Fragment to control HDMI Cec settings.
 */
public class HdmiCecFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "HdmiCecFragment";
    private static HdmiCecFragment mHdmiCecFragment = null;

    private static final String KEY_CEC_SWITCH                  = "key_cec_switch";
    private static final String KEY_CEC_VOLUME_CONTROL          = "key_cec_volume_control";
    private static final String KEY_CEC_ONE_KEY_PLAY            = "key_cec_one_key_play";
    private static final String KEY_CEC_AUTO_POWER_OFF          = "key_cec_auto_power_off";
    private static final String KEY_CEC_AUTO_WAKE_UP            = "key_cec_auto_wake_up";
    private static final String KEY_CEC_AUTO_CHANGE_LANGUAGE    = "key_cec_auto_change_language";
    private static final String KEY_CEC_ARC_SWITCH              = "key_cec_arc_switch";
    private static final String KEY_CEC_DEVICE_LIST             = "key_cec_device_list";

    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecVolumeControlPref;
    private TwoStatePreference mCecOneKeyPlayPref;
    private TwoStatePreference mCecDeviceAutoPowerOffPref;
    private TwoStatePreference mCecAutoWakeupPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private TwoStatePreference mArcSwitchPref;

    private SystemControlManager mSystemControlManager = SystemControlManager.getInstance();
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private AudioConfigManager mAudioConfigManager = null;
    private HdmiCecManager mHdmiCecManager;
    private static long lastObserveredTime = 0;

    public static HdmiCecFragment newInstance() {
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = new HdmiCecFragment();
        }
        return mHdmiCecFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHdmiCecManager = new HdmiCecManager(getContext());
        if (mAudioConfigManager == null) {
            mAudioConfigManager = AudioConfigManager.getInstance(getActivity());
        }
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }
        super.onCreate(savedInstanceState);
    }

    private String[] getArrayString(int resid) {
        return getActivity().getResources().getStringArray(resid);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.hdmicec, null);
        boolean tvFlag = mHdmiCecManager.isTv();
        mCecSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_SWITCH);
        mCecVolumeControlPref = (TwoStatePreference) findPreference(KEY_CEC_VOLUME_CONTROL);
        mCecOneKeyPlayPref = (TwoStatePreference) findPreference(KEY_CEC_ONE_KEY_PLAY);
        mCecDeviceAutoPowerOffPref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_POWER_OFF);
        mCecAutoWakeupPref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_WAKE_UP);
        mCecAutoChangeLanguagePref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_CHANGE_LANGUAGE);
        mArcSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_ARC_SWITCH);

        final Preference hdmiDeviceSelectPref = findPreference(KEY_CEC_DEVICE_LIST);
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = newInstance();
        }
        hdmiDeviceSelectPref.setOnPreferenceChangeListener(mHdmiCecFragment);

        final ListPreference digitalSoundPref = (ListPreference) findPreference(SoundFragment.KEY_DIGITALSOUND_FORMAT);
        digitalSoundPref.setValue(mSoundParameterSettingManager.getDigitalAudioFormat());
        if (tvFlag) {
            /* not support passthrough when ms12 so are not included.*/
            if (!mSoundParameterSettingManager.isAudioSupportMs12System()) {
                String[] entry = getArrayString(R.array.digital_sounds_tv_entries);
                String[] entryValue = getArrayString(R.array.digital_sounds_tv_entry_values);
                List<String> entryList = new ArrayList<String>(Arrays.asList(entry));
                List<String> entryValueList = new ArrayList<String>(Arrays.asList(entryValue));
                entryList.remove("Passthough");
                entryValueList.remove(SoundParameterSettingManager.DIGITAL_SOUND_PASSTHROUGH);
                digitalSoundPref.setEntries(entryList.toArray(new String[]{}));
                digitalSoundPref.setEntryValues(entryValueList.toArray(new String[]{}));
            } else {
                digitalSoundPref.setEntries(R.array.digital_sounds_tv_entries);
                digitalSoundPref.setEntryValues(R.array.digital_sounds_tv_entry_values);
            }
        } else {
            digitalSoundPref.setEntries(R.array.digital_sounds_box_entries);
            digitalSoundPref.setEntryValues(R.array.digital_sounds_box_entry_values);
        }
        digitalSoundPref.setOnPreferenceChangeListener(this);

        final SeekBarPreference audioOutputLatencyPref = (SeekBarPreference) findPreference(SoundFragment.KEY_AUDIO_OUTPUT_LATENCY);
        audioOutputLatencyPref.setOnPreferenceChangeListener(this);
        audioOutputLatencyPref.setMax(AudioConfigManager.HAL_AUDIO_OUT_DEV_DELAY_MAX);
        audioOutputLatencyPref.setMin(AudioConfigManager.HAL_AUDIO_OUT_DEV_DELAY_MIN);
        audioOutputLatencyPref.setSeekBarIncrement(SoundFragment.KEY_AUDIO_OUTPUT_LATENCY_STEP);
        audioOutputLatencyPref.setValue(mAudioConfigManager.getAudioOutputAllDelay());

        mCecSwitchPref.setVisible(true);
        mCecOneKeyPlayPref.setVisible(!tvFlag);
        mCecAutoWakeupPref.setVisible(tvFlag);
        mArcSwitchPref.setVisible(tvFlag);
        mCecDeviceAutoPowerOffPref.setVisible(true);
        mCecAutoChangeLanguagePref.setVisible(!tvFlag);
        hdmiDeviceSelectPref.setVisible(tvFlag);
        audioOutputLatencyPref.setVisible(tvFlag);
        digitalSoundPref.setVisible(false);

        refresh();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) {
            return super.onPreferenceTreeClick(preference);
        }
        switch (key) {
        case KEY_CEC_SWITCH:
            mHdmiCecManager.enableHdmiControl(mCecSwitchPref.isChecked());
            mCecSwitchPref.setEnabled(false);
            enableSubSwitches(false);
            checkEnableSwitch(MSG_ENABLE_CEC_SWITCH);
            return true;
        case KEY_CEC_ONE_KEY_PLAY:
            mHdmiCecManager.enableOneTouchPlay(mCecOneKeyPlayPref.isChecked());
            return true;
        case KEY_CEC_AUTO_POWER_OFF:
            mHdmiCecManager.enableAutoPowerOff(mCecDeviceAutoPowerOffPref.isChecked());
            return true;
        case KEY_CEC_AUTO_WAKE_UP:
            mHdmiCecManager.enableAutoWakeUp(mCecAutoWakeupPref.isChecked());
            return true;
        case KEY_CEC_AUTO_CHANGE_LANGUAGE:
            mHdmiCecManager.enableAutoChangeLanguage(mCecAutoChangeLanguagePref.isChecked());
            return true;
        case KEY_CEC_ARC_SWITCH:
            mHdmiCecManager.enableArc(mArcSwitchPref.isChecked());
            mArcSwitchPref.setEnabled(false);
            checkEnableSwitch(MSG_ENABLE_ARC_SWITCH);
            return true;
        case KEY_CEC_VOLUME_CONTROL:
            updateVolumeControl(mCecVolumeControlPref.isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), SoundFragment.KEY_DIGITALSOUND_FORMAT)) {
            mSoundParameterSettingManager.setDigitalAudioFormat((String)newValue);
        } else if (TextUtils.equals(preference.getKey(), SoundFragment.KEY_AUDIO_OUTPUT_LATENCY)) {
            mAudioConfigManager.setAudioOutputAllDelay((int)newValue);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void updateVolumeControl(boolean enabled) {
        mHdmiCecManager.enableVolumeControl(enabled);
    }

    private void refresh() {
        boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
        mCecSwitchPref.setChecked(hdmiControlEnabled);
        mCecOneKeyPlayPref.setChecked(mHdmiCecManager.isOneTouchPlayEnabled());
        mCecDeviceAutoPowerOffPref.setChecked(mHdmiCecManager.isAutoPowerOffEnabled());
        mCecAutoWakeupPref.setChecked(mHdmiCecManager.isAutoWakeUpEnabled());
        mCecAutoChangeLanguagePref.setChecked(mHdmiCecManager.isAutoChangeLanguageEnabled());
        mCecVolumeControlPref.setChecked(mHdmiCecManager.isVolumeControlEnabled());
        mArcSwitchPref.setChecked(mHdmiCecManager.isArcEnabled());

        enableSubSwitches(hdmiControlEnabled);
    }

    private void checkEnableSwitch(int what) {
        mHandler.sendEmptyMessageDelayed(what, TIME_DELAYED);
        Toast.makeText(getContext(), R.string.cec_wait, Toast.LENGTH_SHORT).show();
    }

    private void enableSubSwitches(boolean hdmiControlEnabled) {
        mCecOneKeyPlayPref.setEnabled(hdmiControlEnabled);
        mCecDeviceAutoPowerOffPref.setEnabled(hdmiControlEnabled);
        mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
        mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);
        mArcSwitchPref.setEnabled(hdmiControlEnabled);
        mCecVolumeControlPref.setEnabled(hdmiControlEnabled);
    }

    private static final int MSG_ENABLE_CEC_SWITCH = 0;
    private static final int MSG_ENABLE_ARC_SWITCH = 1;
    private static final int TIME_DELAYED = 5000;//ms
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_CEC_SWITCH:
                    mCecSwitchPref.setEnabled(true);
                    enableSubSwitches(mCecSwitchPref.isChecked());
                    break;
                case MSG_ENABLE_ARC_SWITCH:
                    mArcSwitchPref.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };
}
