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
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SoundFragment;

import java.util.Map;
import java.util.Set;
import android.os.SystemProperties;
/**
 * Fragment to control HDMI Cec settings.
 */
public class HdmiCecFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener{

    private static final String TAG = "HdmiCecFragment";
    private static HdmiCecFragment mHdmiCecFragment = null;

    private static final String KEY_CEC_SWITCH                    = "cec_switch";
    private static final String KEY_CEC_ONEKEY_PLAY               = "cec_onekey_play";
    private static final String KEY_CEC_DEVICE_AUTO_POWEROFF      = "cec_device_auto_poweroff";
    private static final String KEY_CEC_AUTO_WAKEUP               = "cec_auto_wakeup";
    private static final String KEY_CEC_AUTO_CHANGE_LANGUAGE      = "cec_auto_change_language";
    private static final String KEY_ARC_SWITCH                    = "arc_switch";
    private static final String KEY_DEVICE_SELECT                 = "tv_cec_device_select_list";

    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecOnekeyPlayPref;
    private TwoStatePreference mCecDeviceAutoPoweroffPref;
    private TwoStatePreference mCecAutoWakeupPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private TwoStatePreference mArcSwitchPref;

    private SystemControlManager mSystemControlManager = SystemControlManager.getInstance();
    private SoundParameterSettingManager mSoundParameterSettingManager;
    private HdmiCecManager mHdmiCecManager;

    public static HdmiCecFragment newInstance() {
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = new HdmiCecFragment();
        }
        return mHdmiCecFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHdmiCecManager = new HdmiCecManager(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (mSoundParameterSettingManager == null) {
            mSoundParameterSettingManager = new SoundParameterSettingManager(getActivity());
        }
        setPreferencesFromResource(R.xml.hdmicec, null);
        boolean tvFlag = SettingsConstant.needDroidlogicTvFeature(getContext())
                    && (SystemProperties.getBoolean("tv.soc.as.mbox", false) == false);
        mCecSwitchPref = (TwoStatePreference) findPreference(KEY_CEC_SWITCH);
        mCecOnekeyPlayPref = (TwoStatePreference) findPreference(KEY_CEC_ONEKEY_PLAY);
        mCecDeviceAutoPoweroffPref = (TwoStatePreference) findPreference(KEY_CEC_DEVICE_AUTO_POWEROFF);
        mCecAutoWakeupPref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_WAKEUP);
        mCecAutoChangeLanguagePref = (TwoStatePreference) findPreference(KEY_CEC_AUTO_CHANGE_LANGUAGE);
        mArcSwitchPref = (TwoStatePreference) findPreference(KEY_ARC_SWITCH);
        mCecOnekeyPlayPref.setVisible(!tvFlag);

        int localDeviceType = mSystemControlManager.getPropertyInt("ro.vendor.platform.hdmi.device_type", HdmiCecManager.CEC_LOCAL_DEVICE_TYPE_TV);
        if (HdmiCecManager.CEC_LOCAL_DEVICE_TYPE_TV == localDeviceType ||
                HdmiCecManager.CEC_LOCAL_DEVICE_TYPE_AUDIO == localDeviceType) {
            mCecSwitchPref.setVisible(true);
        } else {
            mCecSwitchPref.setVisible(false);
        }
        final Preference hdmiDeviceSelectPref = findPreference(KEY_DEVICE_SELECT);
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = newInstance();
        }
        hdmiDeviceSelectPref.setOnPreferenceChangeListener(mHdmiCecFragment);
        hdmiDeviceSelectPref.setVisible(true);

        final ListPreference digitalsoundPref = (ListPreference) findPreference(SoundFragment.KEY_DIGITALSOUND_PASSTHROUGH);
        digitalsoundPref.setEntries(getActivity().getResources().getStringArray(R.array.digital_sounds_tv_entries));
        digitalsoundPref.setEntryValues(getActivity().getResources().getStringArray(R.array.digital_sounds_tv_entry_values));
        digitalsoundPref.setValue(mSoundParameterSettingManager.getDigitalAudioFormat());
        digitalsoundPref.setOnPreferenceChangeListener(this);
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
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_CEC_SWITCH, TIME_DELAYED);
            mCecSwitchPref.setEnabled(false);
            mCecOnekeyPlayPref.setEnabled(false);
            mCecDeviceAutoPoweroffPref.setEnabled(false);
            mCecAutoWakeupPref.setEnabled(false);
            mCecAutoChangeLanguagePref.setEnabled(false);
            mArcSwitchPref.setEnabled(false);
            return true;
        case KEY_CEC_ONEKEY_PLAY:
            mHdmiCecManager.enableOneTouchPlay(mCecOnekeyPlayPref.isChecked());
            return true;
        case KEY_CEC_DEVICE_AUTO_POWEROFF:
            mHdmiCecManager.enableAutoPowerOff(mCecDeviceAutoPoweroffPref.isChecked());
            return true;
        case KEY_CEC_AUTO_WAKEUP:
            mHdmiCecManager.enableAutoWakeUp(mCecAutoWakeupPref.isChecked());
            return true;
        case KEY_CEC_AUTO_CHANGE_LANGUAGE:
            mHdmiCecManager.enableAutoChangeLanguage(mCecAutoChangeLanguagePref.isChecked());
            return true;
        case KEY_ARC_SWITCH:
            mHdmiCecManager.enableArc(mArcSwitchPref.isChecked());
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_ARC_SWITCH, TIME_DELAYED);
            mArcSwitchPref.setEnabled(false);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        if (TextUtils.equals(preference.getKey(), SoundFragment.KEY_DIGITALSOUND_PASSTHROUGH)) {
            mSoundParameterSettingManager.setDigitalAudioFormat((String)newValue);
        }
        return true;
    }

    private void refresh() {
        boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
        mCecSwitchPref.setChecked(hdmiControlEnabled);
        mCecOnekeyPlayPref.setChecked(mHdmiCecManager.isOneTouchPlayEnabled());
        mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
        mCecDeviceAutoPoweroffPref.setChecked(mHdmiCecManager.isAutoPowerOffEnabled());
        mCecDeviceAutoPoweroffPref.setEnabled(hdmiControlEnabled);
        mCecAutoWakeupPref.setChecked(mHdmiCecManager.isAutoWakeUpEnabled());
        mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
        mCecAutoChangeLanguagePref.setChecked(mHdmiCecManager.isAutoChangeLanguageEnabled());
        mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);

        boolean arcEnabled = mHdmiCecManager.isArcEnabled();
        mArcSwitchPref.setChecked(arcEnabled);
        mArcSwitchPref.setEnabled(hdmiControlEnabled);
    }

    private static final int MSG_ENABLE_CEC_SWITCH = 0;
    private static final int MSG_ENABLE_ARC_SWITCH = 1;
    private static final int TIME_DELAYED = 2000;//ms
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_CEC_SWITCH:
                    mCecSwitchPref.setEnabled(true);
                    boolean hdmiControlEnabled = mHdmiCecManager.isHdmiControlEnabled();
                    mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
                    mCecDeviceAutoPoweroffPref.setEnabled(hdmiControlEnabled);
                    mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
                    mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);
                    mArcSwitchPref.setEnabled(hdmiControlEnabled);
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
