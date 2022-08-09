package com.droidlogic.tv.settings.sliceprovider.manager;

//import static android.provider.Settings.Global.HDMI_CONTROL_ENABLED;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import android.provider.Settings;
import android.hardware.hdmi.HdmiControlManager;
import android.content.ContentResolver;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;

public class HdmiCecContentManager {
    private static final String TAG = HdmiCecContentManager.class.getSimpleName();

    private Context mContext;
    private ContentResolver mResolver;
    private static volatile HdmiCecContentManager mHdmiCecContentManager;
    private HdmiControlManager mHdmiControlManager;  // This is a system service(HdmiControlManager).

    public static boolean isInit() {
        return mHdmiCecContentManager != null;
    }

    public static HdmiCecContentManager getHdmiCecContentManager(final Context context) {
        if (mHdmiCecContentManager == null) {
            synchronized (HdmiCecContentManager.class) {
            if (mHdmiCecContentManager == null)
                mHdmiCecContentManager = new HdmiCecContentManager(context);
            }
        }
        return mHdmiCecContentManager;
    }

    public static void shutdown(final Context context) {
        if (mHdmiCecContentManager != null) {
            synchronized (HdmiCecContentManager.class) {
                if (mHdmiCecContentManager != null) {
                    mHdmiCecContentManager = null;
                }
            }
        }
    }

    private HdmiCecContentManager(final Context context) {
        mContext = context;
        mResolver = mContext.getContentResolver();
        mHdmiControlManager = mContext.getSystemService(HdmiControlManager.class);
    }

    public boolean isHdmiControlEnabled() {
        boolean hdmiCecEnable = (mHdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED);
        if (MediaSliceUtil.CanDebug()) {
            Log.d(TAG, "isHdmiControlEnabled: " + hdmiCecEnable);
        }
        return hdmiCecEnable;
    }

    public void setHdmiCecEnabled(boolean enable) {
        if (MediaSliceUtil.CanDebug()) {
            Log.d(TAG, "setHdmiCecEnabled cec switch: " + enable);
        }

        mHdmiControlManager.setHdmiCecEnabled(enable
                ? HdmiControlManager.HDMI_CEC_CONTROL_ENABLED
                : HdmiControlManager.HDMI_CEC_CONTROL_DISABLED);

        mResolver.notifyChange(MediaSliceConstants.DISPLAYSOUND_HDMI_CEC_URI, null);
    }


    public String isHdmiControlEnabledName() {
        boolean cecEnabled = mHdmiControlManager.getHdmiCecEnabled()
                == HdmiControlManager.HDMI_CEC_CONTROL_ENABLED;

        return cecEnabled ? "Enabled" : "Disabled";
    }

}
