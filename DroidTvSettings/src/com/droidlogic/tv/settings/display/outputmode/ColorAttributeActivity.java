/*
 * Copyright (c) 2014 Amlogic, Inc. All rights reserved.
 *
 * This source code is subject to the terms and conditions defined in the
 * file 'LICENSE' which is part of this source code package.
 *
 * Description:
 *     AMLOGIC ColorAttributeActivity
 */

package com.droidlogic.tv.settings.display.outputmode;

import androidx.fragment.app.Fragment;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.overlay.FlavorUtils;

/**
 * Activity to control Color space settings.
 */
public class ColorAttributeActivity extends TvSettingsActivity {

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(ColorAttributeFragment.class.getName(), null);
    }

}


