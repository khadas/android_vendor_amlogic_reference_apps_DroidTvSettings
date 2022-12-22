// Copyright 2021 Google Inc. All Rights Reserved.

package com.droidlogic.tv.settings.sliceprovider;

import android.os.Handler;
import android.net.Uri;
import android.util.Log;
import android.media.tv.TvInputInfo;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.content.Context;

import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.TvInputSliceBroadcastReceiver;
import com.droidlogic.tv.settings.sliceprovider.manager.TvInputContentManager;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.app.tv.DroidLogicTvUtils;

import java.util.List;

/* Sample codes to provides Channels and Inputs settings by SliceProvider. In the OEM devices,
 * this SliceProvider can be provided by the Live Channels app too.
 */

public class TvInputSliceProvider extends MediaSliceProvider {
    private static final String TAG = "TvInputSliceProvider";
    private TvInputContentManager mTvInputContentManager;
    private static final String COMMANDACTION = "action.startlivetv.settingui";
    private static final String PACKAGE_DROIDLOGIC_TVINPUT = "com.droidlogic.tvinput";
    private static final String PACKAGE_DROIDLOGIC_DTVKIT = "com.droidlogic.dtvkit.inputsource";
    private static final String PACKAGE_GOOGLE_VIDEOS = "com.google.android.videos";

    private static final int LOGICAL_ADDRESS_AUDIO_SYSTEM = 5;
    private Handler mHandler = new Handler();

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    @Override
    public Slice onBindSlice(Uri sliceUri) {
        Log.d(TAG, "onBindSlice: " + sliceUri);
        switch (MediaSliceUtil.getFirstSegment(sliceUri)) {
            case MediaSliceConstants.CHANNELS_AND_INPUTS_PATH:
                return createChannelsAndInputsSlice(sliceUri);
            default:
                return null;
        }
    }

    @Override
    public void shutdown() {
        mTvInputContentManager.shutdown(getContext());
        mTvInputContentManager = null;
        super.shutdown();
    }

    private Slice createChannelsAndInputsSlice(Uri sliceUri) {
        final Context context = getContext();
        final PreferenceSliceBuilder psb = new PreferenceSliceBuilder(context, sliceUri);
        if (!TvInputContentManager.isInit()) {
            mHandler.post(
                    () -> {
                        mTvInputContentManager = TvInputContentManager
                                .getTvInputContentManager(context);
                        context.getContentResolver().notifyChange(sliceUri, null);
                    });
            Log.d(TAG, "createChannelsAndInputsSlice: notify change!");
            return psb.build();
        }

        if (mTvInputContentManager.getInputSourceSupportList() == null) {
            Log.d(TAG, "Hide Channels & Inputs!");
            psb.addPreference(
                    new RowBuilder()
                            .setTitle("No Signal Source")
                            .setSelectable(false));
            return psb.build();
        }

        psb.addScreenTitle(
                new RowBuilder()
                        .setTitle(context.getString(R.string.channels_and_inputs))
                        .setSubtitle(context.getString(R.string.channels_and_inputs_summary)));
        psb.setEmbeddedPreference(
            new RowBuilder()
                .setTitle(context.getString(R.string.channels_and_inputs)));
        updateChannelsAndInputsDetails(psb);
        return psb.build();
    }

    private void updateChannelsAndInputsDetails(PreferenceSliceBuilder psb) {
        final Context themedContext = TvInputContentManager.mContext;
        List<TvInputInfo> inputSourceSupportList = mTvInputContentManager
                .getInputSourceSupportList();
        List<HdmiDeviceInfo> hdmiList = mTvInputContentManager.getHdmiList();
        HdmiDeviceInfo audioSystem = mTvInputContentManager.getOrigHdmiDevice(
                LOGICAL_ADDRESS_AUDIO_SYSTEM, hdmiList);
        String currentInputSource = mTvInputContentManager.getCurrentInputSource();

        Log.d(TAG, "inputSourceList: " + inputSourceSupportList);
        Log.d(TAG, "currentInputSource: " + currentInputSource);

        for (TvInputInfo input : inputSourceSupportList) {
            android.util.Log.d(TAG, "updateChannelsAndInputsDetails: inputId = " + input.getId());
            psb.addPreference(
                new RowBuilder()
                    .setKey(input.getId())
                    .setTitle(mTvInputContentManager
                            .getTitle(themedContext, input, audioSystem, hdmiList))
                    .addRadioButton(
                        generatePendingIntent(
                            getContext(),
                            MediaSliceConstants.CHANNELS_AND_INPUTS,
                            TvInputSliceBroadcastReceiver.class),
                        input.getId().equals(currentInputSource),
                        getContext().getString(R.string.hdr_resolution_radio_group_name)));
        }
    }
}
