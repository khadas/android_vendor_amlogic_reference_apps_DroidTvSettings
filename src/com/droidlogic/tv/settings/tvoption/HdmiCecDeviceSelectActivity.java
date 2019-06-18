/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description: JAVA file
 */
package com.droidlogic.tv.settings.tvoption;

import com.droidlogic.tv.settings.BaseSettingsFragment;
import com.droidlogic.tv.settings.TvSettingsActivity;

import android.app.Fragment;

/**
 * Activity to control HDMI CEC settings.
 */
public class HdmiCecDeviceSelectActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return SettingsFragment.newInstance();
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final HdmiCecDeviceSelectFragment fragment = HdmiCecDeviceSelectFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
