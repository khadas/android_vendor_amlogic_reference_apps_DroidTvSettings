/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC HdmiCecActivity
 */

package com.droidlogic.tv.settings.tvoption;

import androidx.fragment.app.Fragment;
import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.overlay.FlavorUtils;

/**
 * Activity to control HDMI CEC settings.
 */
public class HdmiCecActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(HdmiCecFragment.class.getName(), null);
    }

}
