/* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.display.dolbyvision;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.preference.SwitchPreference;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.droidlogic.app.DolbyVisionSettingManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.ProgressingDialogUtil;
import com.droidlogic.tv.settings.dialog.old.Action;

import java.util.List;
import java.util.ArrayList;

public class DolbyVisionSettingFragment extends SettingsPreferenceFragment {
    private static final String TAG = "DolbyVisionSettingFragment";

    public static final String KEY_DOLBY_VISION     = "dolby_vision_set";

    private static final int DV_LL_RGB            = 3;
    private static final int DV_LL_YUV            = 2;
    private static final int DV_ENABLE            = 1;
    private static final int DV_DISABLE           = 0;

    private static final String DV_RADIO_GROUP = "dv";
    private static final String DOLBY_VISION_DEFAULT = "dolby_vision_default";
    private static final String DOLBY_VISION_LL_YUV  = "dolby_vision_ll_yuv";
    private static final String DOLBY_VISION_LL_RGB  = "dolby_vision_ll_rgb";
    private static final String DOLBY_VISION_DISABLE = "dolby_vision_disable";

    private DolbyVisionSettingManager mDolbyVisionSettingManager;
    private OutputModeManager mOutputModeManager;
    private ProgressingDialogUtil mProgressingDialogUtil;

    // Adjust this value to keep things relatively responsive without janking
    // animations
    private static final int DV_SET_DELAY_MS = 500;
    private final Handler mDVModeHandler = new Handler();
    private String mNewDvMode;
    private String mOldDVMode;
    Intent serviceIntent;
    private Context themedContext;
    private Bundle mSavedInstanceState;
    private final Runnable mSetDvRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mDolbyVisionSettingManager.isTvSupportDolbyVision().equals("")) {
                mOutputModeManager.setBestDolbyVision(false);
            }
            if (DOLBY_VISION_DEFAULT.equals(mNewDvMode)) {
                mDolbyVisionSettingManager.setDolbyVisionEnable(DV_ENABLE);
                serviceIntent = new Intent(getPreferenceManager().getContext(), DolbyVisionService.class);
                getPreferenceManager().getContext().startService(serviceIntent);
            } else if (DOLBY_VISION_LL_YUV.equals(mNewDvMode)) {
                mDolbyVisionSettingManager.setDolbyVisionEnable(DV_LL_YUV);
                serviceIntent = new Intent(getPreferenceManager().getContext(), DolbyVisionService.class);
                getPreferenceManager().getContext().startService(serviceIntent);
            } else if (DOLBY_VISION_LL_RGB.equals(mNewDvMode)) {
                mDolbyVisionSettingManager.setDolbyVisionEnable(DV_LL_RGB);
                serviceIntent = new Intent(getPreferenceManager().getContext(), DolbyVisionService.class);
                getPreferenceManager().getContext().startService(serviceIntent);
            } /*else if (DOLBY_VISION_DISABLE.equals(mNewDvMode)) {
                mDolbyVisionSettingManager.setDolbyVisionEnable(DV_DISABLE);
                if (serviceIntent != null) {
                    getPreferenceManager().getContext().stopService(serviceIntent);
                }
            }*/
            mUIHandler.sendEmptyMessage(MSG_PLUG_FRESH_UI);
        }
    };

    public static DolbyVisionSettingFragment newInstance() {
        return new DolbyVisionSettingFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mDolbyVisionSettingManager = new DolbyVisionSettingManager((Context) getActivity());
        mOutputModeManager = OutputModeManager.getInstance(getActivity());
        themedContext = getPreferenceManager().getContext();
        mProgressingDialogUtil = new ProgressingDialogUtil();
        mSavedInstanceState = savedInstanceState;
        updatePreferenceFragment(mSavedInstanceState);
    }

    private ArrayList<Action> getActions() {
        boolean enable = mDolbyVisionSettingManager.isDolbyVisionEnable();
        int type = mDolbyVisionSettingManager.getDolbyVisionType();
        String mode = mDolbyVisionSettingManager.isTvSupportDolbyVision();
        String curMode = mOutputModeManager.getCurrentOutputMode();
        ArrayList<Action> actions = new ArrayList<Action>();
        if (mode.equals("")) {
            actions.add(new Action.Builder()
                .key(DOLBY_VISION_DEFAULT)
                .title(getString(R.string.dolby_vision_default_enable))
                .checked((enable == true) && (type == DV_ENABLE))
                .build());
        }
        if (!mode.equals("") && mode.contains("DV_RGB_444_8BIT")) {
            actions.add(new Action.Builder()
                .key(DOLBY_VISION_DEFAULT)
                .title(getString(R.string.dolby_vision_sink_led))
                .checked((enable == true) && (type == DV_ENABLE))
                .build());
        }
        if (!mode.equals("")) {
            if (mode.contains("LL_YCbCr_422_12BIT")) {
                actions.add(new Action.Builder()
                    .key(DOLBY_VISION_LL_YUV)
                    .title(getString(R.string.dolby_vision_low_latency_yuv))
                    .checked((enable == true) && (type == DV_LL_YUV))
                    .build());
            }
            if ((mode.contains("LL_RGB_444_12BIT") || mode.contains("LL_RGB_444_10BIT"))
                    && !curMode.contains("2160") && !curMode.contains("smpte")) {
                actions.add(new Action.Builder()
                    .key(DOLBY_VISION_LL_RGB)
                    .title(getString(R.string.dolby_vision_low_latency_rgb))
                    .checked((enable == true) && (type == DV_LL_RGB))
                    .build());
            }
        }
        /*actions.add(new Action.Builder()
            .key(DOLBY_VISION_DISABLE)
            .title(getString(R.string.dolby_vision_off))
            .checked(enable == false)
            .build());*/
        return actions;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        mOldDVMode = mNewDvMode;
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                mNewDvMode = radioPreference.getKey().toString();
                Log.d(TAG, "mOldDVMode = " + mOldDVMode + ", mNewDvMode = " + mNewDvMode);
                mDVModeHandler.removeCallbacks(mSetDvRunnable);
                mDVModeHandler.post(mSetDvRunnable);

                String newDvModeTitle = radioPreference.getTitle().toString();
                mProgressingDialogUtil.showWarningDialogOnResolutionChange(themedContext, newDvModeTitle,
                        new ProgressingDialogUtil.DialogCallBackInterface() {
                            @Override
                            public void positiveCallBack() {

                            }
                            @Override
                            public void negativeCallBack() {
                                mNewDvMode = mOldDVMode;
                                mDVModeHandler.removeCallbacks(mSetDvRunnable);
                                mDVModeHandler.post(mSetDvRunnable);
                            }
                        });
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void updatePreferenceFragment(Bundle savedInstanceState) {
        Log.d(TAG, "updatePreferenceFragment: updateUI!!");
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.dolby_vision_set);
        Preference activePref = null;

        final List<Action> dvInfoList = getActions();
        for (final Action dvInfo : dvInfoList) {
            final String dvTag = dvInfo.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(dvTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(dvInfo.getTitle());
            radioPreference.setRadioGroup(DV_RADIO_GROUP);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

            if (dvInfo.isChecked()) {
                mNewDvMode = dvTag;
                radioPreference.setChecked(true);
                activePref = radioPreference;
            }
            screen.addPreference(radioPreference);
        }
        if (activePref != null && savedInstanceState == null) {
            scrollToPreference(activePref);
        }
        setPreferenceScreen(screen);
    }

    private static final int MSG_PLUG_FRESH_UI = 0;
    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLUG_FRESH_UI:
                    updatePreferenceFragment(mSavedInstanceState);
                    break;
            }
        }
    };

}
