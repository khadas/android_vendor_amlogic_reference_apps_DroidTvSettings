package com.droidlogic.tv.settings.sliceprovider.dialog;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;
import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager.HdrFormat;
import java.util.concurrent.TimeUnit;

public class AdjustResolutionDialogActivity extends BaseDialogActivity {
    private static final String COUNTDOWN_PLACEHOLDER = "COUNTDOWN_PLACEHOLDER";
    private static final String RESOLUTION_PLACEHOLDER = "RESOLUTION_PLACEHOLDER";
    private static String TAG = AdjustResolutionDialogActivity.class.getSimpleName();
    private static int DEFAULT_COUNTDOWN_SECONDS = 15;
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private CountDownTimer mCountDownTimer;

    private Runnable mRestoreCallback = () -> {};
    private String mNextMode;
    private String mCurrentMode;
    private boolean mWasDolbyVisionChanged;
    private int countdownInSeconds = 0;

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy !!!!!");
        super.onDestroy();
        if (mAlertDialog != null || mAlertDialog.isShowing()) {
            Log.i(TAG, "dismiss dialog !!!!!");
            mAlertDialog.dismiss();
            if (mCountDownTimer != null) {
                Log.i(TAG, "dismiss mCountDownTimer !!!!!");
                mCountDownTimer.cancel();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayCapabilityManager =
                DisplayCapabilityManager.getDisplayCapabilityManager(getApplicationContext());
        mCurrentMode = mDisplayCapabilityManager.getCurrentMode();
        mNextMode = getIntent().getStringExtra(EXTRA_PREFERENCE_KEY);
        Log.d(TAG, "mCurrentMode: " + mCurrentMode + "; mNextMode: " + mNextMode);

        mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(mNextMode);
        mWasDolbyVisionChanged = mDisplayCapabilityManager.adjustDolbyVisionByMode(mNextMode);
        mRestoreCallback =
                () -> {
                    mDisplayCapabilityManager.setResolutionAndRefreshRateByMode(mCurrentMode);
                    if (mWasDolbyVisionChanged) {
                        mDisplayCapabilityManager.setPreferredFormat(HdrFormat.DOLBY_VISION);
                    }
                };

        initAlertDialog();
        showDialog();

    }

  private void initAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogBackground);
    builder.setCancelable(false);
    builder.setPositiveButton(
        R.string.adjust_resolution_dialog_ok_msg,
        (dialog, which) -> {
          if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
          }
          finish();
        });
    builder.setNegativeButton(
        R.string.adjust_resolution_dialog_cancel_msg,
        (dialog, which) -> {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            finish();
            mRestoreCallback.run();
        });

    builder.setTitle(getString(R.string.adjust_resolution_dialog_title));
    builder.setMessage(createWarningMessage());
    mAlertDialog = builder.create();
    mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
  }

  private String createWarningMessage() {
    String msg;
    if (!mWasDolbyVisionChanged) {
      msg = getString(R.string.adjust_resolution_dialog_desc);
    } else {
      msg = getString(R.string.adjust_resolution_and_disable_dv_dialog_desc);
    }

    return msg.replace(RESOLUTION_PLACEHOLDER, mDisplayCapabilityManager.getTitleByMode(mNextMode));
  }

  private void showDialog() {
    mAlertDialog.show();
    mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    countdownInSeconds = DEFAULT_COUNTDOWN_SECONDS;
    mCountDownTimer =
        new CountDownTimer(
            TimeUnit.SECONDS.toMillis(DEFAULT_COUNTDOWN_SECONDS), TimeUnit.SECONDS.toMillis(1L)) {

          final Button cancelButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

          @Override
          public void onTick(long millisUntilFinished) {
            cancelButton.setText(
                getString(R.string.adjust_resolution_dialog_cancel_msg)
                    .replace(COUNTDOWN_PLACEHOLDER, String.valueOf(countdownInSeconds)));
            if (countdownInSeconds != 0) {
              countdownInSeconds--;
            }
          }

          @Override
          public void onFinish() {
            mAlertDialog.dismiss();
            finish();
            mRestoreCallback.run();
          }
        };
    mCountDownTimer.start();
  }

}
