package com.droidlogic.tv.settings.sliceprovider;

import android.net.Uri;

public class MediaSliceConstants {
  public static final String ACTION_MATCH_CONTENT_POLICY_CHANGED =
      "com.google.android.settings.usage.ACTION_MATCH_CONTENT_POLICY_CHANGED";
  public static final String ACTION_SET_HDR_FORMAT =
      "com.google.android.settings.usage.ACTION_SET_HDR_FORMAT";
  public static final String ACTION_SET_COLOR_ATTRIBUTE =
      "com.google.android.settings.usage.ACTION_SET_COLOR_ATTRIBUTE";
  public static final String ACTION_SET_DOLBY_VISION_MODE =
      "com.google.android.settings.usage.ACTION_SET_DOLBY_VISION_MODE";
  public static final String ACTION_SURROUND_SOUND_ENABLED =
      "com.google.android.settings.usage.ACTION_SURROUND_SOUND_ENABLED";
  public static final String ACTION_SOUND_FORMAT_CONTROL_POLICY_CHANGED =
      "com.google.android.settings.usage.ACTION_SOUND_FORMAT_CONTROL_POLICY_CHANGED";
  public static final String ACTION_SET_SOUND_FORMAT =
      "com.google.android.settings.usage.ACTION_SET_SOUND_FORMAT";
  public static final String ACTION_SET_UNSUPPORTED_SOUND_FORMAT =
      "com.google.android.settings.usage.ACTION_SET_UNSUPPORTED_SOUND_FORMAT";
  public static final String ACTION_HDMI_SWITCH_CEC_CHANGED =
      "com.google.android.settings.usage.ACTION_HDMI_SWITCH_CEC_CHANGED";
  public static final String ACTION_HDMI_VOLUME_CONTROL_CHANGED =
      "com.google.android.settings.usage.ACTION_HDMI_VOLUME_CONTROL_CHANGED";

  public static final String ACTION_DEVICE_POWER_BOOT_RESUME =
          "com.google.android.settings.usage.ACTION_DEVICE_POWER_BOOT_RESUME";

  public static final String ACTION_DISPLAY_RESET =
          "android.setting.intent.action.DISPLAY_RESET";

  public static final String SHOW_RESOLUTION_CHNAGE_WARNING =
      "com.google.android.chromecast.chromecastservice.sliceprovider.SHOW_RESOLUTION_CHNAGE_WARNING";
  public static final String SHOW_UNSUPPORTED_FORMAT_CHNAGE_WARNING =
      "com.google.android.chromecast.chromecastservice.sliceprovider.SHOW_UNSUPPORTED_FORMAT_CHNAGE_WARNING";

  public static final String HDR_AUTHORITY = "com.google.android.tv.settings.hdr.sliceprovider";
  public static final String CHANNELS_AND_INPUTS = "com.google.android.tv.settings.vendor.sliceprovider";
  public static final String DEVICE_POWER_BOOT = "com.google.android.tv.settings.powerbehavior.sliceprovider";
  public static final String DISPLAYSOUND_HDMI_AUTHORITY = "com.google.android.tv.settings.displaysound.hdmi.sliceprovider";
  public static final String ADVANCED_SOUND_AUTHORITY =
      "com.google.android.tv.settings.advancedsound.sliceprovider";
  public static final String GENERAL_AUTHORITY =
      "com.google.android.tv.settings.general.sliceprovider";
  public static final String MATCH_CONTENT_PATH = "match_content";
  public static final String ACTION_AUTO_BEST_RESOLUTIONS_ENABLED = "auto_best_resolution";
  public static final String RESOLUTION_PATH = "resolution";
  public static final String DEVICE_POWER_BEHAVIOR_PATH = "power_boot_resume";
  public static final String CHANNELS_AND_INPUTS_PATH = "channels_and_inputs";
  public static final String HDR_AND_COLOR_FORMAT_PATH = "hdr_and_color_format";
  public static final String HDR_FORMAT_PREFERENCE_PATH = "hdr_format_preference";
  public static final String COLOR_ATTRIBUTE_PATH = "color_attribute";
  public static final String DOLBY_VISION_MODE_PATH = "dolby_vision_mode";
  public static final String ADVANCED_SOUND_SETTINGS_PATH = "advanced_sound_settings";
  public static final String SURROUND_SOUND_TOGGLE_PATH = "surround_sound_toggle";
  public static final String ADVANCED_SOUND_SETTINGS_FORMAT_SELECTION_PATH =
      "advanced_sound_settings_format_selection";
  public static final String GENERAL_INFO_PATH = "general_info";
  public static final String HDMI_CEC_PATH = "switch_cec";

  public static final Uri MATCH_CONTENT_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + MATCH_CONTENT_PATH);
  public static final Uri CHANNELS_AND_INPUTS_URI =
          Uri.parse("content://" + CHANNELS_AND_INPUTS + "/" + CHANNELS_AND_INPUTS_PATH);
  public static final Uri DEVICE_POWER_BOOT_URI =
      Uri.parse("content://" + DEVICE_POWER_BOOT + "/" + DEVICE_POWER_BEHAVIOR_PATH);
  public static final Uri RESOLUTION_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + RESOLUTION_PATH);
  public static final Uri HDR_AND_COLOR_FORMAT_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + HDR_AND_COLOR_FORMAT_PATH);
  public static final Uri HDR_FORMAT_PREFERENCE_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + HDR_FORMAT_PREFERENCE_PATH);
  public static final Uri COLOR_ATTRIBUTE_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + COLOR_ATTRIBUTE_PATH);
  public static final Uri DOLBY_VISION_MODE_URI =
      Uri.parse("content://" + HDR_AUTHORITY + "/" + DOLBY_VISION_MODE_PATH);

  public static final Uri SURROUND_SOUND_TOGGLE_URI =
      Uri.parse("content://" + ADVANCED_SOUND_AUTHORITY + "/" + SURROUND_SOUND_TOGGLE_PATH);
  public static final Uri ADVANCED_SOUND_SETTINGS_URI =
      Uri.parse("content://" + ADVANCED_SOUND_AUTHORITY + "/" + ADVANCED_SOUND_SETTINGS_PATH);
  public static final Uri ADVANCED_SOUND_SETTINGS_FORMAT_SELECTION_URI =
      Uri.parse(
          "content://"
              + ADVANCED_SOUND_AUTHORITY
              + "/"
              + ADVANCED_SOUND_SETTINGS_FORMAT_SELECTION_PATH);
  public static final Uri DISPLAYSOUND_HDMI_CEC_URI =
      Uri.parse("content://" + DISPLAYSOUND_HDMI_AUTHORITY + "/" + HDMI_CEC_PATH);
  public static final Uri ESN_URI =
      Uri.parse("content://" + GENERAL_AUTHORITY + "/" + GENERAL_INFO_PATH);
  public static final String PREVIOUS_SURROUND_SOUND_GLOBAL_SETTING =
      "PREVIOUS_SURROUND_SOUND_GLOBAL_SETTING";
  public static final String DOLBY_VISION_MODE_LL_PREFERRED = "DOLBY_VISION_MODE_LL_PREFERRED";


  public static final int MEDIA_DISPLAY_DENSITY_LOW = 120;
  public static final int MEDIA_DISPLAY_DENSITY_MIDDLE = 213;
  public static final int MEDIA_DISPLAY_DENSITY_HIGH = 320;

  public static final int MEDIA_DISPLAY_RESOLUTION_480 = 480;
  public static final int MEDIA_DISPLAY_RESOLUTION_576 = 576;
  public static final int MEDIA_DISPLAY_RESOLUTION_720 = 720;
  public static final int MEDIA_DISPLAY_RESOLUTION_1080 = 1080;
  public static final int MEDIA_DISPLAY_RESOLUTION_1280 = 1280;
  public static final int MEDIA_DISPLAY_RESOLUTION_1920 = 1920;

}
