package com.droidlogic.tv.settings.sliceprovider.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.manager.PowerBehaviorContentManager;

import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;
import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

public class PowerBehaviorSliceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = PowerBehaviorSliceBroadcastReceiver.class.getSimpleName();
    private PowerBehaviorContentManager mPowerBehaviorContentManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "action: " + action + ", key: " + intent.getStringExtra(EXTRA_PREFERENCE_KEY));
        if (MediaSliceConstants.ACTION_DEVICE_POWER_BOOT_RESUME.equals(action)) {
            String key = intent.getStringExtra(EXTRA_PREFERENCE_KEY);
            mPowerBehaviorContentManager = PowerBehaviorContentManager.getPowerBehaviorContentManager(context);
            mPowerBehaviorContentManager.setPowerActionDefinition(key);

            context.getContentResolver().notifyChange(MediaSliceConstants.DEVICE_POWER_BOOT_URI, null);
        }
    }
}
