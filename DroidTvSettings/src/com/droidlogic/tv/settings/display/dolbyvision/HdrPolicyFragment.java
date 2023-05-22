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
import android.os.RemoteException;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;
import android.util.ArrayMap;
import android.util.Log;
import android.text.TextUtils;

import com.droidlogic.app.DolbyVisionSettingManager;
import com.droidlogic.app.OutputModeManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.ProgressingDialogUtil;
import com.droidlogic.tv.settings.dialog.old.Action;
import com.droidlogic.tv.settings.display.outputmode.OutputUiManager;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class HdrPolicyFragment extends SettingsPreferenceFragment {
    private static final String LOG_TAG = "HdrPolicyFragment";

    private static final String HDR_POLICY_SINK   = "hdr_policy_sink";
    private static final String HDR_POLICY_SOURCE = "hdr_policy_source";

    private static final String DV_HDR_SOURCE     = "1";
    private static final String DV_HDR_SINK       = "0";
    private OutputUiManager mOutputUiManager;
    private String mNewHdrPolicy;
    private String mOldHdrPolicy;
    private ProgressingDialogUtil mProgressingDialogUtil;
    private Context themedContext;
    private Bundle mSavedInstanceState;
    // Adjust this value to keep things relatively responsive without janking
    // animations

    public static HdrPolicyFragment newInstance() {
        return new HdrPolicyFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mOutputUiManager = new OutputUiManager((Context) getActivity());
        themedContext = getPreferenceManager().getContext();
        mProgressingDialogUtil = new ProgressingDialogUtil();
        mSavedInstanceState = savedInstanceState;
        updatePreferenceFragment(mSavedInstanceState);
    }

    private ArrayList<Action> getActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder()
            .key(HDR_POLICY_SINK)
            .title(getString(R.string.hdr_policy_sink))
            .checked(mOutputUiManager.getHdrStrategy().equals(DV_HDR_SINK))
            .build());

        actions.add(new Action.Builder()
            .key(HDR_POLICY_SOURCE)
            .title(getString(R.string.hdr_policy_source))
            .checked(mOutputUiManager.getHdrStrategy().equals(DV_HDR_SOURCE))
            .build());

        return actions;
    }
    public boolean onClickHandle(String key) {
        switch (key) {
            case HDR_POLICY_SINK:
                Log.i(LOG_TAG,"checked SINK");
                mOutputUiManager.setHdrStrategy(DV_HDR_SINK);
                break;
            case HDR_POLICY_SOURCE:
                Log.i(LOG_TAG,"checked SOURCE");
                mOutputUiManager.setHdrStrategy(DV_HDR_SOURCE);
                break;
            default:
                Log.i(LOG_TAG,"checked default");
                    return false;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        mOldHdrPolicy = mNewHdrPolicy;
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                mNewHdrPolicy = radioPreference.getKey();
                Log.d(LOG_TAG, "mOldHdrPolicy = " + mOldHdrPolicy + ", mNewHdrPolicy = " + mNewHdrPolicy);
                if (onClickHandle(mNewHdrPolicy) == true) {
                    radioPreference.setChecked(true);
                }
                String newHdrPolicyTitle = radioPreference.getTitle().toString();
                mProgressingDialogUtil.showWarningDialogOnResolutionChange(themedContext, newHdrPolicyTitle,
                        new ProgressingDialogUtil.DialogCallBackInterface() {
                            @Override
                            public void positiveCallBack() {

                            }
                            @Override
                            public void negativeCallBack() {
                                onClickHandle(mOldHdrPolicy);
                                mUIHandler.sendEmptyMessage(MSG_PLUG_FRESH_UI);
                            }
                        });

            } else {
                radioPreference.setChecked(true);
                Log.i(LOG_TAG,"not checked");
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void updatePreferenceFragment(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "updatePreferenceFragment: updateUI!!");
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.device_hdr_policy);
        Preference activePref = null;

        final List<Action> dvInfoList = getActions();
        for (final Action dvInfo : dvInfoList) {
            final String dvTag = dvInfo.getKey();
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(dvTag);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(dvInfo.getTitle());
            //radioPreference.setRadioGroup(DV_RADIO_GROUP);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

            if (dvInfo.isChecked()) {
                mNewHdrPolicy = dvTag;
                radioPreference.setChecked(true);
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
