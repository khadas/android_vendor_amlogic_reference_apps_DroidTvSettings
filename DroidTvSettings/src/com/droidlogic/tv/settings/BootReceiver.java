/*
 * Copyright (C) 2020 The Android Open Source Project
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

import com.droidlogic.tv.settings.sliceprovider.DisplayDensityManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/** The {@BroadcastReceiver} for performing actions upon device boot. */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final boolean DEBUG = false;

    private static final String NATIVE_CONNECTED_DEVICE_SLICE_PROVIDER_URI =
            "content://com.droidlogic.tv.settings.accessories.sliceprovider/general";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "onReceive");
        }

        /*try {
            context.startService(new Intent(context, DisplayDensityManagerService.class));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }*/

        // Start the Service that supports ConnectedDevicesSliceProvider only if the URI is not
        // overlaid.
        if (context != null
                && NATIVE_CONNECTED_DEVICE_SLICE_PROVIDER_URI.equals(
                        context.getResources().getString(R.string.connected_devices_slice_uri))) {
            //Intent mainIntent = new Intent(context, BluetoothDevicesService.class);
            //context.startService(mainIntent);
        }
    }
}