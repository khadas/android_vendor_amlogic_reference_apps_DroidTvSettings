/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC OutputmodeActivity
 */

package com.droidlogic.tv.settings.display.outputmode.displaymode;

import androidx.fragment.app.Fragment;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.overlay.FlavorUtils;
/**
 * Activity to control Output mode settings.
 */
public class Display_1_OutputModeActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(Display_1_OutputModeFragment.class.getName(), null);
    }

}


