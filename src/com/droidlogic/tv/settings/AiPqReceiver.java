/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.droidlogic.tv.settings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.util.DroidUtils;

public class AiPqReceiver extends BroadcastReceiver {
    private SystemControlManager mSystemControlManager;
    @Override
        public void onReceive ( Context cxt, Intent intent ) {
            mSystemControlManager = SystemControlManager.getInstance();
            SharedPreferences DealData = PreferenceManager.getDefaultSharedPreferences(cxt);
            Boolean value = DealData.getBoolean(AiPqFragment.KEY_ENABLE_AIPQ_INFO, false);

            if (value && mSystemControlManager.getAipqEnable()) {
                cxt.startService(new Intent(cxt,AiPqService.class));
            }
        }
}
