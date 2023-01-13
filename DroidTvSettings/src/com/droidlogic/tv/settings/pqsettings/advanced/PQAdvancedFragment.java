/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC PQAdvancedFragment
 */



package com.droidlogic.tv.settings.pqsettings.advanced;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.util.ArrayMap;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.droidlogic.app.DisplayPositionManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.SettingsConstant;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import com.droidlogic.tv.settings.pqsettings.PQSettingsManager;

public class PQAdvancedFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "PQAdvancedFragment";

    private static final String PQ_PICTURE_ADVANCED_DYNAMIC_TONE_MAPPING = "pq_picture_advanced_dynamic_tone_mapping";
    private static final String PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT = "pq_picture_advanced_color_management";
    private static final String PQ_HDMI_COLOR_RANGE = "pq_hdmi_color_range";
    private static final String PQ_PICTURE_ADVANCED_COLOR_SPACE = "pq_picture_advanced_color_space";
    private static final String PQ_PICTURE_ADVANCED_GLOBAL_DIMMING = "pq_picture_advanced_global_dimming";
    private static final String PQ_PICTURE_ADVANCED_LOCAL_DIMMING = "pq_picture_advanced_local_dimming";
    private static final String PQ_PICTURE_ADVANCED_BLACK_STRETCH = "pq_picture_advanced_black_stretch";
    private static final String PQ_PICTURE_ADVANCED_DNLP = "pq_picture_advanced_dnlp";
    private static final String PQ_PICTURE_ADVANCED_LOCAL_CONTRAST = "pq_picture_advanced_local_contrast";
    private static final String PQ_PICTURE_ADVANCED_SR = "pq_picture_advanced_sr";
    private static final String PQ_DNR = "pq_dnr";
    private static final String PQ_PICTURE_ADVANCED_DEBLOCK = "pq_picture_advanced_deblock";
    private static final String PQ_PICTURE_ADVANCED_DEMOSQUITO = "pq_picture_advanced_demosquito";
    private static final String PQ_PICTURE_ADVANCED_DECONTOUR = "pq_picture_advanced_decontour";

    private static final String PQ_PICTURE_ADVANCED_DARK_DETAIL = "pq_picture_advanced_dark_detail";
    private static final String PQ_PICTURE_ADVANCED_VRR = "pq_picture_advanced_vrr";
    private static final String PQ_PICTURE_ADVANCED_GAMMA = "pq_picture_advanced_gamma";
    private static final String PQ_PICTURE_ADVANCED_MANUAL_GAMMA = "pq_picture_advanced_manual_gamma";
    private static final String PQ_PICTURE_ADVANCED_COLOR_TEMPERATURE = "pq_picture_advanced_color_temperature";
    private static final String PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE = "pq_picture_advanced_color_customize";
    private static final String PQ_PICTURE_ADVANCED_MEMC = "pq_picture_advanced_memc";

    private static final String PQ_PICTURE_T3 = "NNNN";
    private static final String PQ_PICTURE_T5 = "T963";

    private static final int PQ_PICTURE_ADVANCED_SOURCE_HDR = 1;

    private PQSettingsManager mPQSettingsManager;

    private Preference pq_brightnessPref;
    private boolean mHasMemc = false;
    private boolean mHasLocalDimming = true;

    private SystemControlManager mSystemControlManager;

    public static PQAdvancedFragment newInstance() {
        return new PQAdvancedFragment();
    }

    public static boolean CanDebug() {
        return SystemProperties.getBoolean("sys.pqsetting.debug", false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pq_picture_advanced, null);
        if (mPQSettingsManager == null) {
            mPQSettingsManager = new PQSettingsManager(getActivity());
        }

        final ListPreference pictureAdvancedDynamicToneMappingPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DYNAMIC_TONE_MAPPING);
        final ListPreference pictureAdvancedColorManagementPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT);
        final ListPreference pictureAdvancedColorRangeModePref = (ListPreference) findPreference(PQ_HDMI_COLOR_RANGE);
        final ListPreference pictureAdvancedColorSpacePref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_COLOR_SPACE);
        final ListPreference pictureAdvancedGlobalDimmingPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_GLOBAL_DIMMING);
        final ListPreference pictureAdvancedLocalDimmingPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_LOCAL_DIMMING);
        final ListPreference pictureAdvancedBlackStretchPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_BLACK_STRETCH);
        final ListPreference pictureAdvancedDNLPPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DNLP);
        final ListPreference pictureAdvancedLocalContrastPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_LOCAL_CONTRAST);
        final ListPreference pictureAdvancedSRPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_SR);
        final ListPreference pictureAdvancedDNRPref = (ListPreference) findPreference(PQ_DNR);
        final ListPreference pictureAdvancedDeBlockPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DEBLOCK);
        final ListPreference pictureAdvancedDeMosquitoPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DEMOSQUITO);
        final ListPreference pictureAdvancedDecontourPref = (ListPreference) findPreference(PQ_PICTURE_ADVANCED_DECONTOUR);

        final TwoStatePreference pictureAdvancedDarkDetailPref = (TwoStatePreference) findPreference(PQ_PICTURE_ADVANCED_DARK_DETAIL);
        final TwoStatePreference pictureAdvancedVRRPref = (TwoStatePreference) findPreference(PQ_PICTURE_ADVANCED_VRR);
        final Preference pictureAdvancedGammaPref = (Preference) findPreference(PQ_PICTURE_ADVANCED_GAMMA);
        final Preference pictureAdvancedManualGammaPref = (Preference) findPreference(PQ_PICTURE_ADVANCED_MANUAL_GAMMA);
        final Preference pictureAdvancedColorTemperaturePref = (Preference) findPreference(PQ_PICTURE_ADVANCED_COLOR_TEMPERATURE);
        final Preference pictureAdvancedColorCustomizePref = (Preference) findPreference(PQ_PICTURE_ADVANCED_COLOR_CUSTOMIZE);
        final Preference pictureAdvancedMemcPref = (Preference) findPreference(PQ_PICTURE_ADVANCED_MEMC);

        mSystemControlManager = SystemControlManager.getInstance();
        mHasMemc = mSystemControlManager.hasMemcFunc();
        if (PQ_PICTURE_ADVANCED_SOURCE_HDR == mPQSettingsManager.GetSourceHdrType()
                && (mPQSettingsManager.getChipVersionInfo() != null
                && PQ_PICTURE_T5 == mPQSettingsManager.getChipVersionInfo())) {
            pictureAdvancedDynamicToneMappingPref.setValueIndex(mPQSettingsManager.getAdvancedDynamicToneMappingStatus());
            pictureAdvancedDynamicToneMappingPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedDynamicToneMappingPref.setVisible(false);
        }

        pictureAdvancedColorManagementPref.setValueIndex(mPQSettingsManager.getAdvancedColorManagementStatus());
        pictureAdvancedColorManagementPref.setOnPreferenceChangeListener(this);
        pictureAdvancedColorRangeModePref.setValueIndex(mPQSettingsManager.getHdmiColorRangeStatus());
        pictureAdvancedColorRangeModePref.setOnPreferenceChangeListener(this);
        pictureAdvancedColorSpacePref.setValueIndex(mPQSettingsManager.getAdvancedColorSpaceStatus());
        pictureAdvancedColorSpacePref.setOnPreferenceChangeListener(this);
        pictureAdvancedGlobalDimmingPref.setValueIndex(mPQSettingsManager.getAdvancedGlobalDimmingStatus());
        pictureAdvancedGlobalDimmingPref.setOnPreferenceChangeListener(this);

        if (mHasLocalDimming) {
            pictureAdvancedLocalDimmingPref.setValueIndex(mPQSettingsManager.getAdvancedLocalDimmingStatus());
            pictureAdvancedLocalDimmingPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedLocalDimmingPref.setVisible(false);
        }

        pictureAdvancedBlackStretchPref.setValueIndex(mPQSettingsManager.getAdvancedBlackStretchStatus());
        pictureAdvancedBlackStretchPref.setOnPreferenceChangeListener(this);
        pictureAdvancedDNLPPref.setValueIndex(mPQSettingsManager.getAdvancedDNLPStatus());
        pictureAdvancedDNLPPref.setOnPreferenceChangeListener(this);
        pictureAdvancedLocalContrastPref.setValueIndex(mPQSettingsManager.getAdvancedLocalContrastStatus());
        pictureAdvancedLocalContrastPref.setOnPreferenceChangeListener(this);
        pictureAdvancedSRPref.setValueIndex(mPQSettingsManager.getAdvancedSRStatus());
        pictureAdvancedSRPref.setOnPreferenceChangeListener(this);
        pictureAdvancedDNRPref.setValueIndex(mPQSettingsManager.getDnrStatus());
        pictureAdvancedDNRPref.setOnPreferenceChangeListener(this);
        pictureAdvancedDeBlockPref.setValueIndex(mPQSettingsManager.getAdvancedDeBlockStatus());
        pictureAdvancedDeBlockPref.setOnPreferenceChangeListener(this);
        pictureAdvancedDeMosquitoPref.setValueIndex(mPQSettingsManager.getAdvancedDeMosquitoStatus());
        pictureAdvancedDeMosquitoPref.setOnPreferenceChangeListener(this);

        if (mPQSettingsManager.getChipVersionInfo() != null
                && (PQ_PICTURE_T5 == mPQSettingsManager.getChipVersionInfo()
                || PQ_PICTURE_T3 == mPQSettingsManager.getChipVersionInfo())) {
            pictureAdvancedDecontourPref.setValueIndex(mPQSettingsManager.getAdvancedDecontourStatus());
            pictureAdvancedDecontourPref.setOnPreferenceChangeListener(this);
        } else {
            pictureAdvancedDecontourPref.setVisible(false);
        }

        if (mPQSettingsManager.HDR_TYPE_DOVI == mPQSettingsManager.GetSourceHdrType()) {
            pictureAdvancedDarkDetailPref.setVisible(true);
            pictureAdvancedDarkDetailPref.setOnPreferenceChangeListener(this);
            pictureAdvancedDarkDetailPref.setChecked(mPQSettingsManager.getDolbyDarkDetail());
            String pictureMode = mPQSettingsManager.getPictureModeStatus();
            if (mPQSettingsManager.STATUS_DARK.equals(pictureMode) || mPQSettingsManager.STATUS_GAME.equals(pictureMode)) {
                pictureAdvancedDarkDetailPref.setEnabled(false);
            }
        } else {
            pictureAdvancedDarkDetailPref.setVisible(false);
        }

        pictureAdvancedVRRPref.setVisible(true);
        pictureAdvancedVRRPref.setOnPreferenceChangeListener(this);
        pictureAdvancedVRRPref.setChecked(mPQSettingsManager.getVrr());
        String pictureMode = mPQSettingsManager.getPictureModeStatus();
        if (!mPQSettingsManager.STATUS_GAME.equals(pictureMode) || !mPQSettingsManager.isHdmi20Status()) {
            pictureAdvancedVRRPref.setEnabled(false);
        }

        pictureAdvancedGammaPref.setVisible(true);
        pictureAdvancedManualGammaPref.setVisible(true);
        pictureAdvancedColorTemperaturePref.setVisible(true);
        pictureAdvancedColorCustomizePref.setVisible(true);

        if (mHasMemc) {
            pictureAdvancedMemcPref.setVisible(true);
        } else {
            pictureAdvancedMemcPref.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        /*switch (preference.getKey()) {
            case PQ_BRIGHTNESS_IN:
                mPQSettingsManager.setBrightness(ZOOMINSTEP);
                break;
        }*/
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (CanDebug()) Log.d(TAG, "[onPreferenceTreeClick] preference.getKey() = " + preference.getKey());
        // Because the type of "PQ_PICTURE_ADVANCED_DARK_DETAIL" is inconsistent with the other preferences,
        // the processing logic is separated separately
        if (TextUtils.equals(preference.getKey(), PQ_PICTURE_ADVANCED_DARK_DETAIL)) {
            mPQSettingsManager.setDolbyDarkDetail((boolean) newValue);
            return true;
        }
        if (TextUtils.equals(preference.getKey(), PQ_PICTURE_ADVANCED_VRR)) {
            mPQSettingsManager.setVrr((boolean) newValue);
            return true;
        }

        final int selection = Integer.parseInt((String)newValue);
        switch (preference.getKey()) {
            case PQ_PICTURE_ADVANCED_DYNAMIC_TONE_MAPPING:
                mPQSettingsManager.setAdvancedDynamicToneMappingStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_MANAGEMENT:
                mPQSettingsManager.setAdvancedColorManagementStatus(selection);
                break;
            case PQ_HDMI_COLOR_RANGE:
                mPQSettingsManager.setHdmiColorRangeValue(selection);
                break;
            case PQ_PICTURE_ADVANCED_COLOR_SPACE:
                mPQSettingsManager.setAdvancedColorSpaceStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_GLOBAL_DIMMING:
                mPQSettingsManager.setAdvancedGlobalDimmingStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_LOCAL_DIMMING:
                mPQSettingsManager.setAdvancedLocalDimmingStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_BLACK_STRETCH:
                mPQSettingsManager.setAdvancedBlackStretchStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_DNLP:
                mPQSettingsManager.setAdvancedDNLPStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_LOCAL_CONTRAST:
                mPQSettingsManager.setAdvancedLocalContrastStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_SR:
                mPQSettingsManager.setAdvancedSRStatus(selection);
                break;
            case PQ_DNR:
                mPQSettingsManager.setDnr(selection);
                break;
            case PQ_PICTURE_ADVANCED_DEBLOCK:
                mPQSettingsManager.setAdvancedDeBlockStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_DEMOSQUITO:
                mPQSettingsManager.setAdvancedDeMosquitoStatus(selection);
                break;
            case PQ_PICTURE_ADVANCED_DECONTOUR:
                mPQSettingsManager.setAdvancedDecontourStatus(selection);
                break;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

}
