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

package com.droidlogic.tv.soundeffectsettings;

import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.provider.Settings;

/**
 * Activity to display displaymode and hdr.
 */
public class SoundModeActivity extends TvSettingsActivity {

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
            final SoundModeFragment fragment = SoundModeFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }

    @Override
    public void onResume() {
        startShowActivityTimer();
        super.onResume();
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_BACK:
                        startShowActivityTimer();
                    break;
                default:
                    break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void startShowActivityTimer () {
        handler.removeMessages(0);

        int seconds = Settings.System.getInt(getContentResolver(), OptionParameterManager.KEY_MENU_TIME, OptionParameterManager.DEFUALT_MENU_TIME);
        if (seconds == 1) {
            seconds = 15;
        } else if (seconds == 2) {
            seconds = 30;
        } else if (seconds == 3) {
            seconds = 60;
        } else if (seconds == 4) {
            seconds = 120;
        } else if (seconds == 5) {
            seconds = 240;
        } else {
            seconds = 0;
        }
        if (seconds > 0) {
            handler.sendEmptyMessageDelayed(0, seconds * 1000);
        } else {
            handler.removeMessages(0);
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            finish();
        }
    };
}
