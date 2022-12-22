package com.droidlogic.tv.settings.sliceprovider.broadcastreceiver;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.manager.TvInputContentManager;

public class TvInputSliceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = TvInputSliceBroadcastReceiver.class.getSimpleName();
    private TvInputContentManager mTvInputContentManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "action: " + action + ", key: " + intent.getStringExtra(EXTRA_PREFERENCE_KEY));
        if (MediaSliceConstants.CHANNELS_AND_INPUTS.equals(action)) {
            String key = intent.getStringExtra(EXTRA_PREFERENCE_KEY);
            mTvInputContentManager = TvInputContentManager.getTvInputContentManager(context);
            mTvInputContentManager.setTvInputSource(key);
            context.getContentResolver().notifyChange(MediaSliceConstants.CHANNELS_AND_INPUTS_URI, null);
        }
    }
}
