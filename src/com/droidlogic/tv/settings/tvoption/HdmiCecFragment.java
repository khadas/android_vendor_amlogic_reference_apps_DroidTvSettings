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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;

import com.droidlogic.app.HdmiCecManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.SettingsConstant;

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

    private static final String PERSIST_HDMI_CEC_SET_MENU_LANGUAGE= "persist.vendor.sys.cec.set_menu_language";
    private static final String PERSIST_HDMI_CEC_DEVICE_AUTO_POWEROFF = "persist.vendor.sys.cec.deviceautopoweroff";
    private TwoStatePreference mCecSwitchPref;
    private TwoStatePreference mCecOnekeyPlayPref;
    private TwoStatePreference mCecDeviceAutoPoweroffPref;
    private TwoStatePreference mCecAutoWakeupPref;
    private TwoStatePreference mCecAutoChangeLanguagePref;
    private TwoStatePreference mArcSwitchPref;

    private SystemControlManager mSystemControlManager = SystemControlManager.getInstance();

    public static HdmiCecFragment newInstance() {
        if (mHdmiCecFragment == null) {
            mHdmiCecFragment = new HdmiCecFragment();
        }
        return mHdmiCecFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) {
            return super.onPreferenceTreeClick(preference);
        }
        switch (key) {
        case KEY_CEC_SWITCH:
            writeCecOption("hdmi_control_enabled"/*Settings.Global.HDMI_CONTROL_ENABLED*/, mCecSwitchPref.isChecked());
            boolean hdmiControlEnabled = readCecOption("hdmi_control_enabled"/*Settings.Global.HDMI_CONTROL_ENABLED*/);
            mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
            mCecDeviceAutoPoweroffPref.setEnabled(hdmiControlEnabled);
            mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
            mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);
            mArcSwitchPref.setEnabled(hdmiControlEnabled);
            return true;
        case KEY_CEC_ONEKEY_PLAY:
            writeCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED, mCecOnekeyPlayPref.isChecked());
            return true;
        case KEY_CEC_DEVICE_AUTO_POWEROFF:
            writeCecOption("hdmi_control_auto_device_off_enabled"/*Settings.Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED*/, mCecDeviceAutoPoweroffPref.isChecked());
            mSystemControlManager.setProperty(PERSIST_HDMI_CEC_DEVICE_AUTO_POWEROFF, mCecDeviceAutoPoweroffPref.isChecked() ? "true" : "false");
            return true;
        case KEY_CEC_AUTO_WAKEUP:
            writeCecOption("hdmi_control_auto_wakeup_enabled", mCecAutoWakeupPref.isChecked());
            return true;
        case KEY_CEC_AUTO_CHANGE_LANGUAGE:
            //writeCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED,
            //      mCecAutoChangeLanguagePref.isChecked());
            mSystemControlManager.setProperty(PERSIST_HDMI_CEC_SET_MENU_LANGUAGE, mCecAutoChangeLanguagePref.isChecked() ? "true" : "false");
            return true;
        case KEY_ARC_SWITCH:
            writeCecOption(HdmiCecManager.HDMI_SYSTEM_AUDIO_CONTROL_ENABLED, mArcSwitchPref.isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //Log.d(TAG, "[onPreferenceChange] preference.getKey() = " + preference.getKey() + ", newValue = " + newValue);
        return true;
    }

    private void refresh() {
        boolean hdmiControlEnabled = readCecOption("hdmi_control_enabled"/*Settings.Global.HDMI_CONTROL_ENABLED*/);
        mCecSwitchPref.setChecked(hdmiControlEnabled);
        mCecOnekeyPlayPref.setChecked(readCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED));
        mCecOnekeyPlayPref.setEnabled(hdmiControlEnabled);
        mCecDeviceAutoPoweroffPref.setChecked(readCecOption("hdmi_control_auto_device_off_enabled"/*Settings.Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED*/));
        mCecDeviceAutoPoweroffPref.setEnabled(hdmiControlEnabled);
        mCecAutoWakeupPref.setChecked(readCecOption("hdmi_control_auto_wakeup_enabled"));
        mCecAutoWakeupPref.setEnabled(hdmiControlEnabled);
        //mCecAutoChangeLanguagePref.setChecked(readCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED));
        mCecAutoChangeLanguagePref.setChecked(mSystemControlManager.getPropertyBoolean(PERSIST_HDMI_CEC_SET_MENU_LANGUAGE, true));
        mCecAutoChangeLanguagePref.setEnabled(hdmiControlEnabled);

        boolean arcEnabled = readCecOption(HdmiCecManager.HDMI_SYSTEM_AUDIO_CONTROL_ENABLED);
        mArcSwitchPref.setChecked(arcEnabled);
        mArcSwitchPref.setEnabled(hdmiControlEnabled);
    }

    private boolean readCecOption(String key) {
        return Settings.Global.getInt(getContext().getContentResolver(), key, 1) == 1;
    }

    private void writeCecOption(String key, boolean value) {
        Settings.Global.putInt(getContext().getContentResolver(), key, value ? 1 : 0);
    }
}
