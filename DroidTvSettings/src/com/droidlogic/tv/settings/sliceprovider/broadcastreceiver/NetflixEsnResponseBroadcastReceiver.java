package com.droidlogic.tv.settings.sliceprovider.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.droidlogic.tv.settings.sliceprovider.MediaSliceConstants;
import com.droidlogic.tv.settings.sliceprovider.manager.GeneralContentManager;

public class NetflixEsnResponseBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = NetflixEsnResponseBroadcastReceiver.class.getSimpleName();
    private static final String NETFLIX_ESN_VALUE_KEY = "ESNValue";

    @Override
    public void onReceive(Context context, Intent intent) {
        String esn = intent.getStringExtra(NETFLIX_ESN_VALUE_KEY);
        String preEsn = GeneralContentManager.getGeneralContentManager(context).getNetflixEsn();
        if (preEsn == null || !preEsn.equals(esn)) {
            GeneralContentManager.getGeneralContentManager(context).setNetflixEsn(esn);
            context.getContentResolver().notifyChange(MediaSliceConstants.ESN_URI, null);
        }
    }
}
