package com.droidlogic.tv.settings.sliceprovider.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.provider.Settings;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiTvClient;
import android.media.tv.TvInputHardwareInfo;

import com.droidlogic.tv.settings.R;
import com.droidlogic.app.DataProviderManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.tv.DroidLogicTvUtils;
import com.droidlogic.app.tv.TvControlManager;
import com.droidlogic.app.tv.TvScanConfig;
import com.droidlogic.app.tv.ChannelInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class TvInputContentManager {
    public static Context mContext;
    private static final String TAG = TvInputContentManager.class.getSimpleName();

    private static volatile TvInputContentManager mTvInputContentManager;
    private static final String POWER_KEY_DEFINITION = "power_key_definition";
    private static final String PERSISTENT_PROPERTY_POWER_KEY_ACTION = "persist.sys.power.key.action";
    private static final String PERSISTENT_PROPERTY_GTV_VERSION_ACTION = "ro.com.google.gmsversion";

    private final InputsComparator mComparator = new InputsComparator();
    private static final String COMMANDACTION = "action.startlivetv.settingui";
    private static final String PACKAGE_DROIDLOGIC_TVINPUT = "com.droidlogic.tvinput";
    private static final String PACKAGE_DROIDLOGIC_DTVKIT = "com.droidlogic.dtvkit.inputsource";
    private static final String PACKAGE_GOOGLE_VIDEOS = "com.google.android.videos";
    private static final String DATA_FROM_TV_INPUT_TABLE = "tv_current_inputid";

    private static final String INPUT_SOURCE_GOOGLE_HOME_KEY = "home";
    private static final String INPUT_ADTV = "ADTV";
    private static final String INPUT_ATV = "ATV";
    private static final String INPUT_DTV = "DTV";
    private static final String INPUT_AV = "AV";
    private static final String INPUT_HDMI = "HDMI";
    private static final String INPUT_HDMI_LOWER = "Hdmi";

    private final String DTVKITSOURCE = "com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19";

    private HdmiTvClient mTvClient;
    private TvInputManager mTvInputManager;
    private HdmiControlManager mHdmiControlManager;
    private static String mCurrentInputId;

    private static final int MSG_GET_CURRENT_INPUT = 2;
    private static final int LOGICAL_ADDRESS_AUDIO_SYSTEM = 5;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public static boolean isInit() {
        return mTvInputContentManager != null;
    }

    private TvInputContentManager(final Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("TvSettings_TvInput");
        mHandlerThread.start();
        mHandler = new InputHandler(mHandlerThread.getLooper());
        mTvInputManager = (TvInputManager)context.getSystemService(Context.TV_INPUT_SERVICE);
        mHdmiControlManager = (HdmiControlManager) context.getSystemService(Context.HDMI_CONTROL_SERVICE);
        if (mHdmiControlManager != null) {
            mTvClient = mHdmiControlManager.getTvClient();
        }
    }

    public static TvInputContentManager getTvInputContentManager(final Context context) {
        if (mTvInputContentManager == null) {
            synchronized (TvInputContentManager.class) {
                if (mTvInputContentManager == null) {
                    mTvInputContentManager = new TvInputContentManager(context);
                }
            }
        }
        return mTvInputContentManager;
    }

    public static void shutdown(final Context context) {
        if (mTvInputContentManager != null) {
            synchronized (TvInputContentManager.class) {
                if (mTvInputContentManager != null) {
                    mTvInputContentManager = null;
                }
            }
        }
    }

    public void setTvInputSource(String UserPreferredSource) {
        if (INPUT_SOURCE_GOOGLE_HOME_KEY.equals(UserPreferredSource)) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mContext.startActivity(homeIntent);
            return;
        }

        List<TvInputInfo> inputList = mTvInputManager.getTvInputList();
        List<HdmiDeviceInfo> hdmiList = getHdmiList();
        HdmiDeviceInfo audioSystem = getOrigHdmiDevice(LOGICAL_ADDRESS_AUDIO_SYSTEM, hdmiList);
        for (TvInputInfo input : inputList) {
            if (UserPreferredSource.equals(input.getId())) {
                DroidLogicTvUtils.setCurrentInputId(mContext, input.getId());
                android.util.Log.d(TAG,
                        "setTvInputSource: isPassthroughInput = " + input.isPassthroughInput()
                        + "\n" + "title = " + getTitle(mContext, input, audioSystem, hdmiList));
                if (!input.isPassthroughInput()) {
                    DroidLogicTvUtils.setSearchInputId(mContext, input.getId(), false);
                    if (TextUtils.equals(getTitle(mContext, input, audioSystem, hdmiList),
                            mContext.getResources().getString(R.string.input_atv))) {
                        DroidLogicTvUtils.setSearchType(mContext,
                                TvScanConfig.TV_SEARCH_TYPE.get(TvScanConfig.TV_SEARCH_TYPE_ATV_INDEX));
                    } else if (TextUtils.equals(getTitle(mContext, input, audioSystem, hdmiList),
                            mContext.getResources().getString(R.string.input_dtv))) {
                        String country = DroidLogicTvUtils.getCountry(mContext);
                        ArrayList<String> dtvList = TvScanConfig.GetTvDtvSystemList(country);
                        DroidLogicTvUtils.setSearchType(mContext, dtvList.get(0));
                    }
                }
                Settings.System.putInt(mContext.getContentResolver(),
                        DroidLogicTvUtils.TV_CURRENT_DEVICE_ID,
                        DroidLogicTvUtils.getHardwareDeviceId(input));

                SystemControlManager mSystemControlManager = SystemControlManager.getInstance();
                if (DTVKITSOURCE.equals(input.getId())) {//DTVKIT SOURCE
                    Log.d(TAG, "DtvKit source");
                    mSystemControlManager.SetDtvKitSourceEnable(1);
                } else {
                    Log.d(TAG, "Not DtvKit source");
                    mSystemControlManager.SetDtvKitSourceEnable(0);
                }
                Intent intent = new Intent(TvInputManager.ACTION_SETUP_INPUTS);
                intent.putExtra("from_tv_source", true);
                intent.putExtra(TvInputInfo.EXTRA_INPUT_ID, input.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
            }
        }
    }

    public String getCurrentInputSource() {
        mHandler.sendEmptyMessage(MSG_GET_CURRENT_INPUT);
        return mCurrentInputId;
    }

    public List<TvInputInfo> getInputSourceSupportList() {
        final Context themedContext = mContext;
        try {
            List<TvInputInfo> inputList = mTvInputManager.getTvInputList();
            Collections.sort(inputList, mComparator);
            inputList.removeIf(tvInputInfo -> tvInputInfo.isHidden(themedContext) ||
                    (tvInputInfo.isPassthroughInput() && tvInputInfo.getParentId() != null));
            return inputList;
        } catch (Exception e) {
            Log.d(TAG, "inputList is "+e);
        }
        return null;
    }

    public List<HdmiDeviceInfo> getHdmiList() {
        if (mTvClient == null) {
            Log.e(TAG, "mTvClient null!");
            return null;
        }
        return mTvClient.getDeviceList();
    }

    private class InputsComparator implements Comparator<TvInputInfo> {
        @Override
        public int compare(TvInputInfo lhs, TvInputInfo rhs) {
            if (lhs == null) {
                return (rhs == null) ? 0 : 1;
            }
            if (rhs == null) {
                return -1;
            }

            int priorityL = getPriority(lhs);
            int priorityR = getPriority(rhs);
            if (priorityL != priorityR) {
                return priorityR - priorityL;
            }

            String customLabelL = (String) lhs.loadCustomLabel(mContext);
            String customLabelR = (String) rhs.loadCustomLabel(mContext);
            if (!TextUtils.equals(customLabelL, customLabelR)) {
                customLabelL = customLabelL == null ? "" : customLabelL;
                customLabelR = customLabelR == null ? "" : customLabelR;
                return customLabelL.compareToIgnoreCase(customLabelR);
            }

            String labelL = (String) lhs.loadLabel(mContext);
            String labelR = (String) rhs.loadLabel(mContext);
            labelL = labelL == null ? "" : labelL;
            labelR = labelR == null ? "" : labelR;
            return labelL.compareToIgnoreCase(labelR);
        }

        private int getPriority(TvInputInfo info) {
            switch (info.getType()) {
                case TvInputInfo.TYPE_TUNER:
                    return 9;
                case TvInputInfo.TYPE_HDMI:
                    HdmiDeviceInfo hdmiInfo = info.getHdmiDeviceInfo();
                    if (hdmiInfo != null && hdmiInfo.isCecDevice()) {
                        return 8;
                    }
                    return 7;
                case TvInputInfo.TYPE_DVI:
                    return 6;
                case TvInputInfo.TYPE_COMPONENT:
                    return 5;
                case TvInputInfo.TYPE_SVIDEO:
                    return 4;
                case TvInputInfo.TYPE_COMPOSITE:
                    return 3;
                case TvInputInfo.TYPE_DISPLAY_PORT:
                    return 2;
                case TvInputInfo.TYPE_VGA:
                    return 1;
                case TvInputInfo.TYPE_SCART:
                default:
                    return 0;
            }
        }
    }

    public HdmiDeviceInfo getOrigHdmiDevice(int logicalAddress, List<HdmiDeviceInfo> hdmiList) {
        if (hdmiList == null) {
            Log.d(TAG, "mTvInputManager or mTvClient maybe null");
            return null;
        }
        for (HdmiDeviceInfo info : hdmiList) {
            if ((info.getLogicalAddress() == logicalAddress)) {
                return info;
            }
        }
        return null;
    }

    public CharSequence getTitle(Context themedContext, TvInputInfo input, HdmiDeviceInfo audioSystem, List<HdmiDeviceInfo> hdmiList) {
        CharSequence title = "";
        CharSequence label = input.loadLabel(themedContext);
        CharSequence customLabel = input.loadCustomLabel(themedContext);
        if (TextUtils.isEmpty(customLabel) || customLabel.equals(label)) {
            title = label;
        } else {
            title = customLabel;
        }
        Log.d(TAG, "getTitle default " + title + ", label = " + label + ", customLabel = " + customLabel);
        android.util.Log.d(TAG, "getTitle: portId = " + DroidLogicTvUtils.getPortId(input));
        if (input.isPassthroughInput()) {
            int portId = DroidLogicTvUtils.getPortId(input);
            if (audioSystem != null && audioSystem.getPortId() == portId) {
                // there is an audiosystem connected.
                title = audioSystem.getDisplayName();
            } else {
                HdmiDeviceInfo hdmiDevice = getOrigHdmiDeviceByPort(portId, hdmiList);
                if (hdmiDevice != null) {
                    // there is a playback connected.
                    title = hdmiDevice.getDisplayName();
                }
            }
        } else if (input.getType() == TvInputInfo.TYPE_TUNER) {
            title = getTitleForTuner(themedContext, input.getServiceInfo().packageName, title, input);
        } else if (TextUtils.isEmpty(title)) {
            title = input.getServiceInfo().name;
        }
        Log.d(TAG, "getTitle " + title);
        return title;
    }

    private HdmiDeviceInfo getOrigHdmiDeviceByPort(int portId, List<HdmiDeviceInfo> hdmiList) {
        if (hdmiList == null) {
            Log.d(TAG, "mTvInputManager or mTvClient maybe null");
            return null;
        }
        for (HdmiDeviceInfo info : hdmiList) {
            if (info.getPortId() == portId) {
                return info;
            }
        }
        return null;
    }

    private CharSequence getTitleForTuner(Context themedContext, String packageName, CharSequence label, TvInputInfo input) {
        CharSequence title = label;
        if (PACKAGE_DROIDLOGIC_TVINPUT.equals(packageName)) {
            title = themedContext.getString(DroidLogicTvUtils.isChina(themedContext) ?
                    R.string.input_atv : R.string.input_long_label_for_tuner);
        } else if (TextUtils.isEmpty(label)) {
            if (PACKAGE_DROIDLOGIC_DTVKIT.equals(packageName)) {
                title = themedContext.getString(R.string.input_dtv_kit);
            } else if (PACKAGE_GOOGLE_VIDEOS.equals(packageName)) {
                title = themedContext.getString(R.string.input_google_channel);
            } else {
                title = input.getServiceInfo().name;
            }
        }
        Log.d(TAG, "getTitleForTuner title " + title + " for package " + packageName);
        return title;
    }

    private class InputHandler extends Handler {
        private InputHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_CURRENT_INPUT:
                    mCurrentInputId = DataProviderManager.getStringValue(
                            mContext, DATA_FROM_TV_INPUT_TABLE, null);
                    break;
                default:
                    break;
            }
        }
    }
}
