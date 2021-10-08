/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License.
 */

package com.droidlogic.tv.settings;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.overlay.FlavorUtils;

/**
 * Activity that allows the enabling and disabling of sound effects.
 */
public class TvSourceActivity extends TvSettingsActivity {

    private static final String TAG = "TvSourceActivity";

    @Override
    protected Fragment createSettingsFragment() {
        return FlavorUtils.getFeatureFactory(this).getSettingsFragmentProvider()
            .newSettingsFragment(TvSourceFragment.class.getName(), null);
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TV_INPUT) {
            Log.d(TAG, "consume KeyEvent.KEYCODE_TV_INPUT here");
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
}
