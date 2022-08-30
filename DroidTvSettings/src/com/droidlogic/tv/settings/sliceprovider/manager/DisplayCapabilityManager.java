package com.droidlogic.tv.settings.sliceprovider.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import com.droidlogic.app.DolbyVisionSettingManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.ueventobserver.SetModeUEventObserver;
import com.droidlogic.tv.settings.sliceprovider.utils.MediaSliceUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.view.WindowManager;
import android.view.Window;
import android.view.Display;
import android.view.Display.Mode;
import android.hardware.display.DisplayManager;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.os.ServiceManager;
import android.os.UserHandle;

public class DisplayCapabilityManager {
  private static final String TAG = DisplayCapabilityManager.class.getSimpleName();
  private static final String VAL_HDR_POLICY_SINK = "0";
  private static final String VAL_HDR_POLICY_SOURCE = "1";
  private static final String DISPLAY_MODE_TRUE = "true";
  private static final String DISPLAY_MODE_FALSE = "false";
  private static final String UBOOTENV_HDR_POLICY = "ubootenv.var.hdr_policy";
  private static final String ENV_IS_BEST_MODE = "ubootenv.var.is.bestmode";
  private static final String SYSTEM_PROPERTY_HDR_PREFERENCE = "persist.vendor.sys.hdr_preference";
  private static final String HDR_CAP_PATH = "/sys/class/amhdmitx/amhdmitx0/hdr_cap";
  private static final String HDR_CAP2_PATH = "/sys/class/amhdmitx/amhdmitx0/hdr_cap2";

  private static final String SUPPORT_HDR1 = "HDR10Plus Supported: 1";
  private static final String SUPPORT_HDR2 = "Traditional SDR: 1";
  private static final String SUPPORT_HDR3 = "SMPTE ST 2084: 1";
  private static final String SUPPORT_HDR4 = "Hybrid Log-Gamma: 1";

  public static final String CVBS_MODE = "cvbs";

  private static final int DV_DISABLE = 0;
  private static final int DV_ENABLE = 1;
  private static final int DV_LL_YUV = 2;

  private static String tvSupportDolbyVisionMode;
  private static String tvSupportDolbyVisionType;

  private static final String[] DOLBY_VISION_TYPE = {
           "DV_RGB_444_8BIT",
           "LL_YCbCr_422_12BIT",
           "LL_RGB_444_12BIT",
           "LL_RGB_444_10BIT"
  };

  // Mode is defined as resolution & frequency (refresh rate) combination
  private static final List<String> HDMI_MODE_LIST =
      ImmutableList.of(
          "2160p60hz",
          "2160p50hz",
          "2160p30hz",
          "2160p25hz",
          "2160p24hz",
          "smpte24hz",
          "1080p60hz",
          "1080p50hz",
          "1080p24hz",
          "720p60hz",
          "720p50hz",
          "576p50hz",
          "480p60hz",
          "576cvbs",
          "480cvbs");

  private static final List<String> HDMI_MODE_TITLE_LIST =
      ImmutableList.of(
          "4k 60Hz",
          "4k 50Hz",
          "4k 30Hz",
          "4k 25Hz",
          "4k 24Hz",
          "4k smpte",
          "1080p 60Hz",
          "1080p 50Hz",
          "1080p 24Hz",
          "720p 60Hz",
          "720p 50Hz",
          "576p 50Hz",
          "480p 60Hz",
          "576 CVBS",
          "480 CVBS");

  private static final List<String> HDMI_COLOR_LIST =
      ImmutableList.of(
          "420,8bit",
          "420,10bit",
          "420,12bit",
          "422,12bit", // 422 8bit & 10bit do not provide lower data rate, so we skip them
          "444,8bit",
          "444,10bit",
          "444,12bit",
          "rgb,8bit",
          "rgb,10bit",
          "rgb,12bit");

  private static final Set<String> HDMI_DEEP_COLOR_SET =
      ImmutableSet.of(
          "420,10bit",
          "420,12bit",
          "422,12bit", // 422 8bit & 10bit do not provide lower data rate, so we skip them
          "444,10bit",
          "444,12bit",
          "rgb,10bit",
          "rgb,12bit");

  private static final List<String> HDMI_COLOR_TITLE_LIST =
      ImmutableList.of(
          "YCbCr 4:2:0 8-bit",
          "YCbCr 4:2:0 10-bit",
          "YCbCr 4:2:0 12-bit",
          "YCbCr 4:2:2 12-bit",
          "YCbCr 4:4:4 8-bit",
          "YCbCr 4:4:4 10-bit",
          "YCbCr 4:4:4 12-bit",
          "RGB 8-bit",
          "RGB 10-bit",
          "RGB 12-bit");

  private static final ImmutableMap<String, Display.Mode> USER_PREFERRED_MODE_BY_MODE =
          new ImmutableMap.Builder<String, Display.Mode>()
              .put("2160p60hz", new Display.Mode(3840, 2160, 60.000004f))
              .put("2160p59.94hz", new Display.Mode(3840, 2160, 59.94006f))
              .put("2160p50hz", new Display.Mode(3840, 2160, 50.0f))
              .put("2160p30hz", new Display.Mode(3840, 2160, 30.000002f))
              .put("2160p29.97hz", new Display.Mode(3840, 2160, 29.97003f))
              .put("2160p25hz", new Display.Mode(3840, 2160, 25.0f))
              .put("2160p24hz", new Display.Mode(3840, 2160, 24.000002f))
              .put("2160p23.976hz", new Display.Mode(3840, 2160, 23.976025f))
              .put("smpte24hz", new Display.Mode(4096, 2160, 24.000002f))
              .put("smpte23.976hz", new Display.Mode(4096, 2160, 23.976025f))
              .put("1080p60hz", new Display.Mode(1920, 1080, 60.000004f))
              .put("1080p59.94hz", new Display.Mode(1920, 1080, 59.94006f))
              .put("1080p50hz", new Display.Mode(1920, 1080, 50.0f))
              .put("1080p24hz", new Display.Mode(1920, 1080, 24.000002f))
              .put("1080p23.976hz", new Display.Mode(1920, 1080, 23.976025f))
              .put("720p60hz", new Display.Mode(1280, 720, 60.000004f))
              .put("720p59.94hz", new Display.Mode(1280, 720, 59.94006f))
              .put("720p50hz", new Display.Mode(1280, 720, 50.0f))
              .put("576p50hz", new Display.Mode(720, 576, 50.0f))
              .put("480p60hz", new Display.Mode(720, 480, 60.000004f))
              .put("480p59.94hz", new Display.Mode(720, 480, 59.94006f))
              .put("576cvbs", new Display.Mode(720, 576, 50.0f))
              .put("480cvbs", new Display.Mode(720, 480, 60.000004f))
              .build();

  private static final Map<String, String> MODE_TITLE_BY_MODE = new HashMap<>();
  private static final Map<String, String> COLOR_TITLE_BY_ATTR = new HashMap<>();

  private final ReadWriteLock mRWLock = new ReentrantReadWriteLock();

  static {
    for (int i = 0; i < HDMI_MODE_LIST.size(); i++) {
      MODE_TITLE_BY_MODE.put(HDMI_MODE_LIST.get(i), HDMI_MODE_TITLE_LIST.get(i));
    }
    for (int i = 0; i < HDMI_COLOR_LIST.size(); i++) {
      COLOR_TITLE_BY_ATTR.put(HDMI_COLOR_LIST.get(i), HDMI_COLOR_TITLE_LIST.get(i));
    }
  }

  public enum HdrFormat {
    DOLBY_VISION("Dolby Vision", "dolby_vision", 0),
    HDR("HDR", "hdr", 1),
    SDR("SDR", "sdr", 2);

    String key;
    String sysProp;
    int order;

    HdrFormat(String _key, String _sysProp, int _order) {
      key = _key;
      sysProp = _sysProp;
      order = _order;
    }

    public static HdrFormat fromKey(String s) {
      for (HdrFormat h : values()) {
        if (h.key.equals(s)) return h;
      }
      return null;
    }

    public String getKey() {
      return key;
    }

    public static HdrFormat fromSysProp(String sysProp) {
      for (HdrFormat h : values()) {
        if (h.sysProp.equals(sysProp)) return h;
      }
      return null;
    }

    public String getSysProp() {
      return sysProp;
    }

    public int getOrder() {
      return order;
    }

    public boolean supports(HdrFormat hdrFormat) {
      return order >= hdrFormat.order;
    }
  }

  private static volatile DisplayCapabilityManager mDisplayCapabilityManager;

  private volatile List<String> mHdmiModeList;
  private volatile List<String> mDolbyVisionModeList;
  private volatile List<String> mHdmiColorAttributeList;
  private volatile List<String> mHdmiDeepColorAttributeList;

  private final SystemControlManager mSystemControlManager;
  private final OutputModeManager mOutputModeManager;
  private final DolbyVisionSettingManager mDolbyVisionSettingManager;
  private final SetModeUEventObserver mSetModeUEventObserver;
  private final ContentResolver mContentResolver;

  private boolean mIsHdr10Supported = false;

  private DisplayManager mDisplayManager;
  // private DisplayDensityManager mDisplayDensityManager;

  public static boolean isInit() {
    return mDisplayCapabilityManager != null;
  }

  public static DisplayCapabilityManager getDisplayCapabilityManager(final Context context) {
    if (mDisplayCapabilityManager == null)
      synchronized (DisplayCapabilityManager.class) {
        if (mDisplayCapabilityManager == null)
          mDisplayCapabilityManager = new DisplayCapabilityManager(context);
      }
    return mDisplayCapabilityManager;
  }

  private DisplayCapabilityManager(final Context context) {
    mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
    // mDisplayDensityManager = new DisplayDensityManager(mDisplayManager);
    mSystemControlManager = SystemControlManager.getInstance();
    mOutputModeManager = new OutputModeManager(context);
    mDolbyVisionSettingManager = new DolbyVisionSettingManager(context);
    mContentResolver = context.getContentResolver();
    mSetModeUEventObserver = SetModeUEventObserver.getInstance();
    mSetModeUEventObserver.setOnUEventRunnable(() -> notifyModeChange(mContentResolver));
    mSetModeUEventObserver.startObserving();
    refresh();
  }

  private void notifyModeChange(ContentResolver contentResolver) {
    contentResolver.notifyChange(MediaSliceConstants.RESOLUTION_URI, null);
    contentResolver.notifyChange(MediaSliceConstants.HDR_AND_COLOR_FORMAT_URI, null);
    contentResolver.notifyChange(MediaSliceConstants.HDR_FORMAT_PREFERENCE_URI, null);
    contentResolver.notifyChange(MediaSliceConstants.MATCH_CONTENT_URI, null);
    contentResolver.notifyChange(MediaSliceConstants.COLOR_ATTRIBUTE_URI, null);
    contentResolver.notifyChange(MediaSliceConstants.DOLBY_VISION_MODE_URI, null);
  }

  public void notifyChangeSlice(ContentResolver contentResolver) {
    notifyModeChange(contentResolver);
  }

  public static void shutdown() {
    if (mDisplayCapabilityManager != null) {
      synchronized (DisplayCapabilityManager.class) {
        if (mDisplayCapabilityManager != null) {
          mDisplayCapabilityManager.stop();
          mDisplayCapabilityManager = null;
        }
      }
    }
  }

  private void stop() {
    mSetModeUEventObserver.stopObserving();
  }

  /**
   * Refresh display capabilities.
   *
   * @return whether or not the display capabilities have changed
   */
  public boolean refresh() {
    boolean updated = false;
    updated |= updateHdmiModes();
    updated |= updateDolbyVisionModes();
    updated |= updateHdmiColorAttributes();
    updated |= updateSupportForHdr10();
    return updated;
  }

  private boolean updateHdmiModes() {
    List<String> preModeList = null;
    if (mHdmiModeList!= null) {
      preModeList = new ArrayList<>(mHdmiModeList);
    }

    final String strEdid = mOutputModeManager.getHdmiSupportList();
    if (MediaSliceUtil.CanDebug()) {
        Log.d(TAG, "getHdmiSupportList:" + strEdid);
    }

    if (strEdid != null && strEdid.length() != 0 && !strEdid.contains("null")) {
      final List<String> edidKeyList = new ArrayList<>();
      for (int i = 0; i < HDMI_MODE_LIST.size(); i++) {
        if (strEdid.contains(HDMI_MODE_LIST.get(i))) {
          edidKeyList.add(HDMI_MODE_LIST.get(i));
        }
      }
      mHdmiModeList = edidKeyList;
    } else {
      mHdmiModeList = HDMI_MODE_LIST;
    }

    Lock lock = mRWLock.writeLock();
    lock.lock();
    filterNoSupportMode(mHdmiModeList);
    lock.unlock();

    return preModeList == null || !preModeList.equals(mHdmiModeList);
  }
  private boolean isContainsInFW(String sysCtrlMode) {
    // CVBS mode does not do mode display contrast filtering.
    if (isCvbsMode()) {
      return true;
    }
    Display.Mode[] frameworkSupportedModes = mDisplayManager.getDisplay(0).getSupportedModes();
    Map<String, Display.Mode> modeMapTmp = USER_PREFERRED_MODE_BY_MODE;
    if (MediaSliceUtil.CanDebug()) {
      Log.d(TAG, "framework support mode: " + Arrays.toString(frameworkSupportedModes));
    }

    boolean matched = false;
    for (Display.Mode mode2 : frameworkSupportedModes) {
      if (mode2.matches(
              modeMapTmp.get(sysCtrlMode).getPhysicalWidth(),
              modeMapTmp.get(sysCtrlMode).getPhysicalHeight(),
              modeMapTmp.get(sysCtrlMode).getRefreshRate())) {
        matched = true;
        break;
      }
    }

    return matched;
  }

  private void filterNoSupportMode(List<String> systemControlModeList) {
    Iterator<String> sysHdmiModeIterator = systemControlModeList.iterator();
    while (sysHdmiModeIterator.hasNext()) {
      String hdmiModeTmp = filterHdmiModes(sysHdmiModeIterator.next());
      if (!hdmiModeTmp.isEmpty() && !isContainsInFW(hdmiModeTmp)) {
        sysHdmiModeIterator.remove();
      }
    }
    if (MediaSliceUtil.CanDebug()) {
      Log.d(TAG, "filterNoSupportModeList: " + systemControlModeList);
    }
  }

  private String filterHdmiModes(String filterHdmiMode) {
    Log.d(TAG, "filterHdmiMode: " + filterHdmiMode);
    if (!mOutputModeManager.getFrameRateOffset().contains("1")
            || filterHdmiMode == null) {

      return filterHdmiMode;
    }

    String filterHdmiModeStr = filterHdmiMode;
    if (filterHdmiMode.contains("60Hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("60Hz", "59.94Hz");
    } else if (filterHdmiMode.contains("30Hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("30Hz", "29.97Hz");
    } else if (filterHdmiMode.contains("24Hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("24Hz", "23.976Hz");
    } else if (filterHdmiMode.contains("60hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("60hz", "59.94hz");
    } else if (filterHdmiMode.contains("30hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("30hz", "29.97hz");
    } else if (filterHdmiMode.contains("24hz")) {
      filterHdmiModeStr = filterHdmiMode.replace("24hz", "23.976hz");
    }

    Log.d(TAG, "filterHdmiModeStr: " + filterHdmiModeStr);
    return filterHdmiModeStr;
  }

  private boolean updateDolbyVisionModes() {
    List<String> preList = mDolbyVisionModeList;
    String highestDolbyVisionSupportedMode = getHighestDolbyVisionMode();
    if (highestDolbyVisionSupportedMode.isEmpty()) {
      mDolbyVisionModeList = new ArrayList<>();
      return !mDolbyVisionModeList.equals(preList);
    }
    final String strEdid = mOutputModeManager.getHdmiSupportList();
    if (strEdid != null && strEdid.length() != 0 && !strEdid.contains("null")) {
      mDolbyVisionModeList = new ArrayList<>();
      // modeValue is a long indicating the related position among all modes.
      // This value is bigger if the resolution/frequency of the mode is higher.
      // We prioritized resolution over frequency here.
      long highestDolbyVisionSupportedModeValue =
          mDolbyVisionSettingManager.resolveResolutionValue(highestDolbyVisionSupportedMode);

      for (String mode : mHdmiModeList) {
        if (mode.contains("smpte") || mode.contains("i")) {
          continue;
        }

        // We assume all modes with lower resolution/frequency than highest supported mode can also
        // be supported
        if (mDolbyVisionSettingManager.resolveResolutionValue(mode)
            <= highestDolbyVisionSupportedModeValue) {
          // Currently available Dolby Vision types (DV_ENABLE, DV_LL_YUV) only allow
          // some color attributes.
          // If a mode is not supported under these color attributes, then it is not supported in
          // Dolby Vision.
          if (doesModeSupportColor(mode, "444,8bit")
              || doesModeSupportColor(mode, "422,12bit")
              || doesModeSupportColor(mode, "422,10bit")) {
            mDolbyVisionModeList.add(mode);
          }
        }
      }
    } else {
      mDolbyVisionModeList = HDMI_MODE_LIST;
    }
    return !mDolbyVisionModeList.equals(preList);
  }

  /**
   * Get highest supported Dolby Vision mode. The order is decided by the resolution, then the
   * frequency.
   *
   * @return highest Dolby Vision mode. It contains the resolution, the frequency, and the color
   *     attributes supported.
   */
  private String getHighestDolbyVisionMode() {
    final String dvCap = mDolbyVisionSettingManager.isTvSupportDolbyVision();
    if (dvCap.isEmpty()) {
      return "";
    }
    for (final String s : HDMI_MODE_LIST) {
      if (dvCap.contains(s)) {
        return s;
      }
    }
    return "";
  }

  private boolean updateHdmiColorAttributes() {
    List<String> preList = mHdmiColorAttributeList;

    List<String> hdmiColorAttrList = new ArrayList<>();
    List<String> hdmiDeepColorAttrList = new ArrayList<>();
    // TODO: check. Does mOutputModeManager.getHdmiColorSupportList() returns the same list when in
    // SDR/HDR/DV mode?
    String strColorList = mOutputModeManager.getHdmiColorSupportList();
    if (strColorList != null && strColorList.length() != 0 && !strColorList.contains("null")) {
      for (String hdmiColor : HDMI_COLOR_LIST) {
        if (strColorList.contains(hdmiColor)) {
          hdmiColorAttrList.add(hdmiColor);
          //if (HDMI_DEEP_COLOR_SET.contains(hdmiColor)) {
            hdmiDeepColorAttrList.add(hdmiColor);
          //}
        }
      }
    }
    mHdmiColorAttributeList = hdmiColorAttrList;
    mHdmiDeepColorAttributeList = hdmiDeepColorAttrList;

    return !mHdmiColorAttributeList.equals(preList);
  }

  private boolean updateSupportForHdr10() {
    final String hdrCap2 = mSystemControlManager.readSysFs(HDR_CAP2_PATH);
    // "SMPTE ST 2084" with value "1" represents the support of HDR10.
    // If it is not supported, the value will be "0"
    // TODO: check. Does mSystemControlManager.readSysFs returns the same value under DV/HDR/SDR
    // preferred?
    // TODO: do we have to read HLG?
    if (MediaSliceUtil.CanDebug()) Log.d(TAG,"updateSupportForHdr10 hdrCap2:"+ hdrCap2);
    boolean hdr10SupportedInCap = hdrCap2.contains(SUPPORT_HDR1)  || hdrCap2.contains(SUPPORT_HDR2) || hdrCap2.contains(SUPPORT_HDR3) || hdrCap2.contains(SUPPORT_HDR4);
    boolean isChanged = mIsHdr10Supported != hdr10SupportedInCap;
    mIsHdr10Supported = hdr10SupportedInCap;
    return isChanged;
  }

  public boolean isHdrPolicySource() {
    final String adaptiveHdr =
        mSystemControlManager.getBootenv(UBOOTENV_HDR_POLICY, VAL_HDR_POLICY_SINK);
    return adaptiveHdr.equals(VAL_HDR_POLICY_SOURCE);
  }

  public void setHdrPolicySource(final boolean isSource) {
    if (isSource) mSystemControlManager.setHdrStrategy(VAL_HDR_POLICY_SOURCE);
    else mSystemControlManager.setHdrStrategy(VAL_HDR_POLICY_SINK);
  }

  public List<String> getHdmiModeLists() {
    return mHdmiModeList;
  }

  public List<String> getHdmiTitleLists() {
    // Need to use a new list to receive the return value of Arrays.asList,
    // because its return value object does not override the add and remove methods.
    return new ArrayList(Arrays.asList(getHdmiModes()));
  }

  public String[] getHdmiModes() {
    Lock lock = mRWLock.readLock();
    lock.lock();
    String[] modeList = mHdmiModeList.toArray(new String[0]);
    lock.unlock();
    return modeList;
  }

  public boolean isCvbsMode() {
    String outputMode = mOutputModeManager.getCurrentOutputMode();
    Log.d(TAG,"isCvbsMode currentMode = " + outputMode);
    if (outputMode.contains(CVBS_MODE)) {
      return true;
    }
    return false;
  }

  public String getTitleByMode(String mode) {
    String filterHdmiMode = filterHdmiModes(MODE_TITLE_BY_MODE.get(mode));
    return filterHdmiMode;
  }

  public boolean isBestResolution() {
        return mOutputModeManager.isBestOutputmode();
  }

  public void change2BestMode(String mode) {
    // Interface abandoned
    // mOutputModeManager.setBestMode(mode);
  }

  public void change2BestMode() {
    if (!DISPLAY_MODE_TRUE.equals(mSystemControlManager.getBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_FALSE))) {
      mSystemControlManager.setBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_TRUE);
    }
    String systemBestOutputMode = mSystemControlManager.getPrefHdmiDispMode();
    setUserPreferredDisplayMode(systemBestOutputMode);
  }

  /**
   * Return titles of the selected mode. Mode is constructed of resolution and refresh rate, so the
   * return values are resolution title and refresh rate title.
   *
   * @return Array contains 2 elements. [0] for resolution title, [1] for refresh rate title
   */
  public String[] getTitlesByMode(String mode) {
    return getTitleByMode(mode).split(" ");
  }

  public String getCurrentMode() {
    return mOutputModeManager.getCurrentOutputMode().trim();
  }

  public void setResolutionAndRefreshRateByMode(final String mode) {
    if (!DISPLAY_MODE_FALSE.equals(mSystemControlManager.getBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_TRUE))) {
      mSystemControlManager.setBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_FALSE);
    }
    setUserPreferredDisplayMode(mode);
  }

  /**
   * The resolution is set using apis in the framework, and
   * before setting the resolution, it is necessary to distinguish
   * whether it is the best resolution.
   * @param mode The resolution to be set
   * @param beastMode Whether to boot the best resolution
   */
  private void setUserPreferredDisplayMode(String userSetMode) {
    Log.i(TAG, "userSetMode: " + userSetMode);
    String mode = filterHdmiModes(userSetMode);

    // The framework filters when the system is at the current resolution, so use SystemControl to set it.
    // Note: Mode filtering is not required when the systemcontrol is used to set resolution
    boolean isSystemHdmiDispMode = checkSysCurrentMode(mDisplayManager.getDisplay(0).getMode(), getPreferredByMode(mode));
    if (isSystemHdmiDispMode) {
      mSystemControlManager.setMboxOutputMode(userSetMode);
      Log.d(TAG, "setMboxOutputMode");
      return;
    }

    String  envIsBestMode = mSystemControlManager.getBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_TRUE);
    Display.Mode[] supportedModes = mDisplayManager.getDisplay(0).getSupportedModes();

    if (MediaSliceUtil.CanDebug()) {
      Log.d(TAG, " envIsBestMode: " + envIsBestMode);
      Log.d(TAG, "supportedModes: " + Arrays.toString(supportedModes));
    }

    Display.Mode matcherMode = checkUserPreferredMode(supportedModes, USER_PREFERRED_MODE_BY_MODE.get(mode));
    Display.Mode userPreferredDisplayMode = mDisplayManager.getGlobalUserPreferredDisplayMode();

    if (userPreferredDisplayMode != null) {
      Log.w(TAG, "userPreferredDisplayMode: " + userPreferredDisplayMode);
      if (checkSysCurrentMode(userPreferredDisplayMode, matcherMode)) {
          mDisplayManager.clearGlobalUserPreferredDisplayMode();
      }
    }
    Log.d(TAG, "matcherMode: " + matcherMode);

    // set resolution
    mDisplayManager.setGlobalUserPreferredDisplayMode(matcherMode);

    /**
     * set density, Unset the density after setting the resolution,
     * and the density is assigned to DroidLogic.
     */
    // mDisplayDensityManager.adjustDisplayDensityByMode(modeMap.get(mode));
  }

  private Display.Mode getPreferredByMode(String userSetMode) {
    Map<String, Display.Mode> modeMap = USER_PREFERRED_MODE_BY_MODE;
    return modeMap.get(userSetMode);
  }

  private boolean checkSysCurrentMode(Display.Mode sysMode, Display.Mode userSetMode) {
    if (sysMode.matches(
            userSetMode.getPhysicalWidth(),
            userSetMode.getPhysicalHeight(),
            userSetMode.getRefreshRate())) {
      return true;
    }
    return false;
  }

  private Display.Mode checkUserPreferredMode(Display.Mode[] modeArr, Display.Mode mode) {
    for (Display.Mode mode2 : modeArr) {
      /*In cvbs mode, the value of the resolution rate reported by hwc to the framework is fake
      data (Google[b/229605079] needs to filter 16:9 mode), so only fps verification is performed in this mode
      (the width and height of cvbs mode are determined, only fps is unique).
      * */
      boolean refreshRate = Float.floatToIntBits(mode2.getRefreshRate()) == Float.floatToIntBits(mode.getRefreshRate());
      if ((isCvbsMode() && refreshRate)
          || (!isCvbsMode()
              &&(mode2.matches(
                      mode.getPhysicalWidth(),
                      mode.getPhysicalHeight(),
                      mode.getRefreshRate())))) {
        return mode2;
      }
    }

    throw new IllegalArgumentException("Unrecognized user preferred mode, invalid mode!!");
  }

  private void autoSelectColorAttributeByMode(String mode, boolean isBestMode) {
    List<String> list;
    if (getPreferredFormat() == HdrFormat.SDR) {
      list = mHdmiColorAttributeList;
    } else {
      list = mHdmiDeepColorAttributeList;
    }
    for (String attr : list) {
      if (doesModeSupportColor(mode, attr)) {
        setColorAttribute(attr, isBestMode);
        return;
      }
    }
  }

  private void setColorAttribute(String str, boolean isBestMode) {
    mOutputModeManager.setDeepColorAttribute(str);
    if (isBestMode) {
      mOutputModeManager.setBestMode(getCurrentMode());
    }
  }

  /** Prioritize the color attribute with the lowest data rate */
  private void autoSelectColorAttribute() {
    List<String> colorAttrList =
        getPreferredFormat() == HdrFormat.SDR
            ? mHdmiColorAttributeList
            : mHdmiDeepColorAttributeList;

    for (String attr : colorAttrList) {
      if (doesModeSupportColor(mOutputModeManager.getCurrentOutputMode(), attr)) {
        setColorAttribute(attr);
        return;
      }
    }
  }

  //hdr10:1 sdr:2 dolby_vision:0
  public void setHdrPriority(HdrFormat hdrFormat) {
    if (MediaSliceUtil.CanDebug()) Log.d(TAG,"setHdrPriority hdrFormat:"+hdrFormat);
    if (hdrFormat == HdrFormat.DOLBY_VISION) {
        mOutputModeManager.setHdrPriority(HdrFormat.DOLBY_VISION.getOrder());
    } else if (hdrFormat == HdrFormat.HDR){
        mOutputModeManager.setHdrPriority(HdrFormat.HDR.getOrder());
    } else if (hdrFormat == HdrFormat.SDR){
        mOutputModeManager.setHdrPriority(HdrFormat.SDR.getOrder());
    }
  }

  public HdrFormat getHdrPriority() {
    int type = mOutputModeManager.getHdrPriority();
    if (MediaSliceUtil.CanDebug()) Log.d(TAG,"getHdrPriority type:"+type);
    if ( type == HdrFormat.DOLBY_VISION.getOrder()) {
        return HdrFormat.DOLBY_VISION;
    } else if ( type == HdrFormat.HDR.getOrder()){
        return HdrFormat.HDR;
    } else if ( type == HdrFormat.SDR.getOrder()){
        return HdrFormat.SDR;
    } else{
        return HdrFormat.DOLBY_VISION;
    }
  }

  public void setColorAttribute(String colorAttr) {
    mOutputModeManager.setDeepColorAttribute(colorAttr);
    mOutputModeManager.setOutputMode(getCurrentMode());
  }

  public String getCurrentColorAttribute() {
    return mOutputModeManager.getCurrentColorAttribute();
  }

  public String getTitleByColorAttr(String attr) {
    attr = attr.trim();
    return COLOR_TITLE_BY_ATTR.get(attr);
  }

  public boolean doesModeSupportColor(String mode, String attr) {
    return mOutputModeManager.isModeSupportColor(mode, attr);
  }

  public void restoreDefaultDisplayMode() {
    //mOutputModeManager.restoreDefaultOutputMode();
  }

  public boolean doesModeSupportDolbyVision(String mode) {
    return mDolbyVisionModeList.contains(mode);
  }

  public void toggleDolbyVision(final boolean enabled) {
    if (!doesModeSupportDolbyVision(getCurrentMode()) && enabled) {
      throw new IllegalArgumentException(
          "Current mode does not support Dolby Vision but receive action Dolby Vision enabled.");
    }

    if (!enabled) {
      mOutputModeManager.setBestDolbyVision(false);
      setDolbyVisionEnable(DV_DISABLE);
      autoSelectColorAttribute();
    } else {
      if (doesDolbyVisionSupportLL()) {
        setDolbyVisionEnable(DV_LL_YUV);
      } else {
        setDolbyVisionEnable(DV_ENABLE);
      }
      mOutputModeManager.setBestDolbyVision(true);
    }
  }

  private HdrFormat getHdrPreference() {
    HdrFormat hdrPreference =
        HdrFormat.fromSysProp(mSystemControlManager.getProperty(SYSTEM_PROPERTY_HDR_PREFERENCE));
    // Use Dolby Vision as default
    return hdrPreference != null ? hdrPreference : getHdrPriority();
  }

  private void setHdrPreference(HdrFormat hdrPreference) {
    mSystemControlManager.setProperty(SYSTEM_PROPERTY_HDR_PREFERENCE, hdrPreference.getSysProp());
  }

  private boolean isDolbyVisionSupported() {
    if (MediaSliceUtil.CanDebug()) Log.d(TAG,"isDolbyVisionSupported isDolbyVisionEnable:"+isDolbyVisionEnable()+" isTvSupportDolbyVision:"+isTvSupportDolbyVision());
    return isDolbyVisionEnable() && isTvSupportDolbyVision();
  }

  public static class HdrFormatConfig {
    private final ImmutableList<HdrFormat> mSupportedFormats;
    private final ImmutableList<HdrFormat> mUnsupportedFormats;

    public HdrFormatConfig(final boolean isHdr10Supported, final boolean isDolbyVisionSupported) {
      List<HdrFormat> supportedFormats = new ArrayList<>();
      List<HdrFormat> unsupportedFormats = new ArrayList<>();
      if (isDolbyVisionSupported) {
        supportedFormats.add(HdrFormat.DOLBY_VISION);
      } else {
        unsupportedFormats.add(HdrFormat.DOLBY_VISION);
      }
      if (isHdr10Supported) {
        // TODO: currently supporting HDR means supporting HDR10, but we have to check HLG later
        supportedFormats.add(HdrFormat.HDR);
      } else {
        unsupportedFormats.add(HdrFormat.HDR);
      }
      supportedFormats.add(HdrFormat.SDR);
      mSupportedFormats = ImmutableList.copyOf(supportedFormats);
      mUnsupportedFormats = ImmutableList.copyOf(unsupportedFormats);
    }

    public List<HdrFormat> getSupportedFormats() {
      return mSupportedFormats;
    }

    public List<HdrFormat> getUnsupportedFormats() {
      return mUnsupportedFormats;
    }
  }

  public HdrFormatConfig getHdrFormatConfig() {
    return new HdrFormatConfig(mIsHdr10Supported, isDolbyVisionSupported());
  }

  public HdrFormat getPreferredFormat() {
    HdrFormat hdrPreference = getHdrPreference();
    if (MediaSliceUtil.CanDebug()) Log.d(TAG,"getPreferredFormat hdrPreference:"+hdrPreference);
    if (hdrPreference.supports(HdrFormat.DOLBY_VISION) && isDolbyVisionSupported() && hdrPreference == HdrFormat.DOLBY_VISION) {
      return HdrFormat.DOLBY_VISION;
    } else if (((hdrPreference.supports(HdrFormat.HDR) && hdrPreference == HdrFormat.HDR) || hdrPreference == HdrFormat.DOLBY_VISION) && mIsHdr10Supported) {
      return HdrFormat.HDR;
    }
    return HdrFormat.SDR;
  }

  public void setPreferredFormat(HdrFormat hdrFormat) {
    setHdrPreference(hdrFormat);
    /*mSetModeUEventObserver.resetObserved();
    if (hdrFormat == HdrFormat.DOLBY_VISION) {
        toggleDolbyVision(true);
    } else if (hdrFormat == HdrFormat.HDR || hdrFormat == HdrFormat.SDR) {
        toggleDolbyVision(false);
    }
    if (!mSetModeUEventObserver.isObserved()) {
        setResolutionAndRefreshRateByMode(getCurrentMode());
    }*/
  }

  public boolean adjustDolbyVisionByMode(String mode) {
    if (!doesModeSupportDolbyVision(mode) && HdrFormat.DOLBY_VISION == getPreferredFormat()) {
      setPreferredFormat(HdrFormat.HDR);
      return true;
    }
    return false;
  }

  public List<String> getColorAttributes() {
    HdrFormat hdrFormat = getPreferredFormat();
    switch (hdrFormat) {
      case HDR:
        return mHdmiDeepColorAttributeList;
      case SDR:
        return mHdmiColorAttributeList;
      default:
        throw new IllegalArgumentException(
            "Only under HDR & SDR preference should we call get color formats");
    }
  }

  public void setModeSupportingDolbyVision() {
    if (!isDolbyVisionSupported()) {
      throw new IllegalArgumentException("There's no mode supporting Dolby Vision.");
    }
    String mode = "1080p60hz";
    if (doesModeSupportDolbyVision(mode)) {
      setResolutionAndRefreshRateByMode(mode);
    } else {
      mode = mDolbyVisionModeList.get(0);
      setResolutionAndRefreshRateByMode(mode);
    }
  }

  public boolean isDolbyVisionModeLLPreferred() {
    return mDolbyVisionSettingManager.getDolbyVisionType() == DV_LL_YUV
            && doesDolbyVisionSupportLL();
    //return doesDolbyVisionSupportLL();
  }

  public void setDolbyVisionModeLLPreferred(boolean preferred) {
    if (preferred && !doesDolbyVisionSupportLL()
        || !preferred && !doesDolbyVisionSupportStandard()) {
      throw new IllegalArgumentException("Selected mode is unsupported.");
    }
    if (preferred) {
      setDolbyVisionEnable(DV_LL_YUV);
    } else {
      setDolbyVisionEnable(DV_ENABLE);
    }
    mOutputModeManager.setBestDolbyVision(false);
  }

  public void setDolbyVisionEnable(final int state) {
    if (!DISPLAY_MODE_FALSE.equals(mSystemControlManager.getBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_TRUE))) {
      mSystemControlManager.setBootenv(ENV_IS_BEST_MODE, DISPLAY_MODE_FALSE);
    }
    mSystemControlManager.setDolbyVisionEnable(state);
    String systemPrefHdmiDispMode = mSystemControlManager.getPrefHdmiDispMode();
    String currentMode = getCurrentMode();

    if (MediaSliceUtil.CanDebug()) {
      Log.d(TAG, "state: " + state + " systemPrefHdmiDispMode: " + systemPrefHdmiDispMode + " currentMode: " + currentMode);
    }

    setUserPreferredDisplayMode(systemPrefHdmiDispMode);
  }

  public boolean doesDolbyVisionSupportLL() {
    String mode = mDolbyVisionSettingManager.isTvSupportDolbyVision();
    return !mode.isEmpty() && mode.contains("LL_YCbCr_422_12BIT");
  }

  public boolean doesDolbyVisionSupportStandard() {
    String mode = mDolbyVisionSettingManager.isTvSupportDolbyVision();
    return !mode.isEmpty()
        && (mode.contains("DV_RGB_444_8BIT") || !mode.contains("LL_YCbCr_422_12BIT"));
  }

  public boolean isTvSupportDolbyVision() {
      String dv_cap = mDolbyVisionSettingManager.isTvSupportDolbyVision();
      tvSupportDolbyVisionType = null;
      if (!dv_cap.equals("")) {
          for (int i = 0;i < HDMI_MODE_LIST.size(); i++) {
              if (dv_cap.contains(HDMI_MODE_LIST.get(i))) {
                  tvSupportDolbyVisionMode = HDMI_MODE_LIST.get(i);
                  break;
              }
          }
          for (int i = 0; i < DOLBY_VISION_TYPE.length; i++) {
              if (dv_cap.contains(DOLBY_VISION_TYPE[i])) {
                  tvSupportDolbyVisionType += DOLBY_VISION_TYPE[i];
              }
          }
      } else {
          tvSupportDolbyVisionMode = "";
          tvSupportDolbyVisionType = "";
      }

      return tvSupportDolbyVisionMode.equals("") ? false : true;
  }

  public boolean isDolbyVisionEnable() {
     return mDolbyVisionSettingManager.isDolbyVisionEnable();
  }
}
