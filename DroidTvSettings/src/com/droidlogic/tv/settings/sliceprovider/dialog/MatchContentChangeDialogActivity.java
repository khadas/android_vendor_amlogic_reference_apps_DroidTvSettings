package com.droidlogic.tv.settings.sliceprovider.dialog;

import static com.android.tv.twopanelsettings.slices.SlicesConstants.EXTRA_PREFERENCE_KEY;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.manager.DisplayCapabilityManager;
import java.util.concurrent.TimeUnit;

public class MatchContentChangeDialogActivity extends Activity {
    private static final String COUNTDOWN_PLACEHOLDER = "COUNTDOWN_PLACEHOLDER";
    private static String TAG = MatchContentChangeDialogActivity.class.getSimpleName();
    private static int DEFAULT_COUNTDOWN_SECONDS = 15;
    private DisplayCapabilityManager mDisplayCapabilityManager;
    private CountDownTimer mCountDownTimer;
    private AlertDialog mAlertDialog;
    private Runnable mRestoreCallback = () -> {};

    private int countdownInSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String key = getIntent().getStringExtra(EXTRA_PREFERENCE_KEY);
        boolean isSource = getApplicationContext().getString(R.string.hdr_match_content_source_key).equals(key);
        setHdrPolicySource(isSource);

        mRestoreCallback =
                () -> {
                    setHdrPolicySource(!isSource);
                };
        initAlertDialog();
        showDialog();
    }

    private void initAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogBackground);
        builder.setCancelable(false);
        builder.setPositiveButton(
                R.string.adjust_color_format_dialog_ok_msg,
                (dialog, which) -> {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    finish();
                });
        builder.setNegativeButton(
                R.string.adjust_color_format_dialog_cancel_msg,
                (dialog, which) -> {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    mRestoreCallback.run();
                    finish();
                });

        builder.setTitle(getString(R.string.adjust_match_content_dialog_title));
        builder.setMessage(getString(R.string.adjust_match_content_dialog_desc));
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
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
                                getString(R.string.adjust_color_format_dialog_cancel_msg)
                                        .replace(COUNTDOWN_PLACEHOLDER, String.valueOf(countdownInSeconds)));
                        if (countdownInSeconds != 0) {
                            countdownInSeconds--;
                        }
                    }

                    @Override
                    public void onFinish() {
                        mAlertDialog.dismiss();
                        mRestoreCallback.run();
                        finish();
                    }
                };
        mCountDownTimer.start();
    }

    private void setHdrPolicySource(boolean isSource) {
        DisplayCapabilityManager.getDisplayCapabilityManager(getApplicationContext()).setHdrPolicySource(isSource);
        getApplicationContext()
                .getContentResolver()
                .notifyChange(MediaSliceConstants.HDR_AND_COLOR_FORMAT_URI, null);
        getApplicationContext().getContentResolver().notifyChange(MediaSliceConstants.MATCH_CONTENT_URI, null);
    }

}
