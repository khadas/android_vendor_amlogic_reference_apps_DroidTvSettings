package com.droidlogic.tv.settings.sliceprovider;

import android.net.Uri;
import android.util.Log;

import androidx.slice.Slice;
import androidx.slice.SliceProvider;

import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.PowerBehaviorSliceBroadcastReceiver;
import com.droidlogic.tv.settings.sliceprovider.manager.PowerBehaviorContentManager;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;

import android.content.ContentResolver;
import android.provider.Settings;

import java.util.HashMap;

public class PowerBehaviorSliceProvider extends MediaSliceProvider {
    private static final String TAG = PowerBehaviorSliceProvider.class.getSimpleName();
    private PowerBehaviorContentManager mPowerBehaviorContentManager;

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    @Override
    public Slice onBindSlice(final Uri sliceUri) {
        Log.d(TAG, "onBindSlice: " + sliceUri);
        switch (MediaSliceUtil.getFirstSegment(sliceUri)) {
            case MediaSliceConstants.DEVICE_POEWRBEHAVOIR_PATH:
                // fill in Netfilx Esn into general info purposely
                return createPowerBehaviorSlice(sliceUri);
            default:
                return null;
        }
    }

    @Override
    public void shutdown() {
        mPowerBehaviorContentManager.shutdown(getContext());
        mPowerBehaviorContentManager = null;
        super.shutdown();
    }

    private Slice createPowerBehaviorSlice(Uri sliceUri) {
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(getContext(), sliceUri);
        psb.addScreenTitle(
                new RowBuilder().setTitle(getContext().getString(R.string.power_and_energy)));

        if (!PowerBehaviorContentManager.isInit()) {
            mPowerBehaviorContentManager = PowerBehaviorContentManager.getPowerBehaviorContentManager(getContext());
        }

        psb.setEmbeddedPreference(
                new RowBuilder()
                    .setTitle(getContext().getString(R.string.power_on_behavior)));
        updatePowerBehaviorDetails(psb);
        return psb.build();
    }

    private void updatePowerBehaviorDetails(PreferenceSliceBuilder psb) {
        HashMap<String, Integer> powerBehavioModeList = mPowerBehaviorContentManager.getpowerBehavioModeList();
        int currentPowerBehavioMode = mPowerBehaviorContentManager.getCurrentBehavior();

        // shutdown mode is currently not supported in the GTV version
        if (mPowerBehaviorContentManager.hasGtvsUiMode()) {
            Log.d(TAG, "this is GTVS, not support shutdown");
            powerBehavioModeList.remove("Shutdown");
        }

        Log.d(TAG, "powerBehavioModeList: " + powerBehavioModeList);
        Log.d(TAG, "currentPowerBehavioMode: " + currentPowerBehavioMode);

        for (String powerBehavioMode : powerBehavioModeList.keySet()) {
            psb.addPreference(
                new RowBuilder()
                    .setKey(powerBehavioMode)
                    .setTitle(powerBehavioMode)
                    .addRadioButton(
                        generatePendingIntent(
                            getContext(),
                            MediaSliceConstants.ACTION_DEVICE_POWER_BOOT_RESUME,
                                PowerBehaviorSliceBroadcastReceiver.class),
                        powerBehavioModeList.get(powerBehavioMode).equals(currentPowerBehavioMode),
                        getContext().getString(R.string.hdr_resolution_radio_group_name)));
        }


    }
}