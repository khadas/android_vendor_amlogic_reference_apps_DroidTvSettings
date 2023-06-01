// Copyright 2021 Google Inc. All Rights Reserved.

package com.droidlogic.tv.settings.sliceprovider;

import android.os.Handler;
import android.net.Uri;
import android.util.Log;
import android.media.tv.TvInputInfo;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder;
import com.android.tv.twopanelsettings.slices.builders.PreferenceSliceBuilder.RowBuilder;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.broadcastreceiver.TvInputSliceBroadcastReceiver;
import com.droidlogic.tv.settings.sliceprovider.manager.TvInputContentManager;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;

import java.util.List;

/* Sample codes to provides Channels and Inputs settings by SliceProvider. In the OEM devices,
 * this SliceProvider can be provided by the Live Channels app too.
 */

public class TvInputSliceProvider extends MediaSliceProvider {
    private static final String TAG = "TvInputSliceProvider";
    private TvInputContentManager mTvInputContentManager;

    private static final String INPUT_SOURCE_GOOGLE_HOME_KEY = "home";
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

        updateInputGoogTvHome(psb, currentInputSource); // add Google Tv home source
        for (TvInputInfo input : inputSourceSupportList) {
            Log.d(TAG, "updateChannelsAndInputsDetails: inputId = " + input.getId());
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

    private void updateInputGoogTvHome(PreferenceSliceBuilder psb, String currentInputSource) {
        psb.addPreference(
            new RowBuilder()
                .setKey(INPUT_SOURCE_GOOGLE_HOME_KEY)
                .setTitle(isBasicMode(getContext())
                    ? getContext().getString(R.string.channels_and_inputs_home_title)
                    : getContext().getString(R.string.channels_and_inputs_home_google_title))
                .addRadioButton(
                    generatePendingIntent(
                        getContext(),
                        MediaSliceConstants.CHANNELS_AND_INPUTS,
                        TvInputSliceBroadcastReceiver.class),
                    INPUT_SOURCE_GOOGLE_HOME_KEY.equals(currentInputSource),
                    getContext().getString(R.string.hdr_resolution_radio_group_name)));
    }

    public boolean isBasicMode(Context context) {
        final String SETTINGS_PACKAGE_NAME = "com.android.tv.settings";
        String providerUriString = "";
        try {
            Resources resources = context.getPackageManager()
                    .getResourcesForApplication(SETTINGS_PACKAGE_NAME);
            int id = resources.getIdentifier("basic_mode_provider_uri", "string", SETTINGS_PACKAGE_NAME);
            if (id != 0) {
                providerUriString = resources.getString(id);
            }
        } catch (Exception e) {
            return false;
        }
        if (TextUtils.isEmpty(providerUriString)) {
            Log.e(TAG, "ContentProvider for basic mode is undefined.");
            return false;
        }
        // The string "offline_mode" is a static protocol and should not be changed in general.
        final String KEY_BASIC_MODE = "offline_mode";
        try {
            Uri contentUri = Uri.parse(providerUriString);
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                String basicMode = cursor.getString(cursor.getColumnIndex(KEY_BASIC_MODE));
                return "1".equals(basicMode);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Unable to query the ContentProvider for basic mode.", e);
            return false;
        }
        return false;
    }
}
