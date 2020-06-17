/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.droidlogic.app.SystemControlManager;

import java.util.HashMap;
import java.util.Random;

public class AiPqService extends Service {
    private final IBinder mBinder = new AiPqBinder();
    public static final HashMap<String, String> mScene = new HashMap<String, String>();
    public static int mThreadId = -1;
    private static final int EVENT_UPDATE_UI = 1;
    private static boolean enabled;
    private AIRemoteView mRemoteView;
    private Handler mHandler;
    private static ReadingThreadLooper mThread;
    private static final String OTHER_SCENE = "7";
    private static final String SCENE_FS = "/sys/class/video/cur_ai_scenes";
    private SystemControlManager mSystemControlManager;

    public AiPqService() {
        initScienceTree();
        mSystemControlManager = SystemControlManager.getInstance();
    }

    private void initScienceTree() {
        if (mScene.size() == 0) {
            mScene.put("0", "skin");
            mScene.put("1", "blue");
            mScene.put("2", "colorful");
            mScene.put("3", "architecture");
            mScene.put("4", "greenish");
            mScene.put("5", "nightsope");
            mScene.put("6", "document");
            mScene.put("7", "OTHER");
        }
        mRemoteView = AIRemoteView.getInstance();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case EVENT_UPDATE_UI:
                        String text = (String) msg.obj;
                        updateRemoveView(text);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class AiPqBinder extends Binder {
        AiPqService getService() {
            return AiPqService.this;
        }
    }

    /* public void hideRemoveView(){
         mRemoteView.hide();
         mThreadId = -1;
     }*/
    private void updateRemoveView(String value) {
        mRemoteView.updateUI(value);
    }

    public boolean isShowing() {
        return mRemoteView != null && mRemoteView.isShow();
    }

    public void enableAipq() {
        Log.d("V", "enableAipq" + enabled);
        if (!enabled) {
            enabled = true;
            showAipqTopView(true);
            if (mThread != null) {
                mThread.stopReading();
            }
            Random r = new Random(System.currentTimeMillis());
            mThreadId = r.nextInt();
            mThread = new ReadingThreadLooper(AiPqService.this, mThreadId);
            mThread.start();
        }
    }

    public void disableAipq() {
        Log.d("V", "disableAipq" + enabled);
        if (mThread != null) {
            Log.d("V", "mThread.threadId" + mThread.threadId);
        } else {
            Log.d("V", "thread is null");
        }
        if (enabled) {
            enabled = false;
            mThreadId = -1;
            if (mThread != null) {
                mThread.stopReading();
            }
            showAipqTopView(false);
        }
    }

    public void updateUI(String ValStr) {
        // Log.d("V","service.mThreadId"+mThreadId+"updateUI"+ValStr);
        String newVal = updateAipqValue(ValStr);
        if (newVal == null || newVal.length() <= 0) {
            newVal = AiPqService.this.getResources().getString(R.string.prepare);
        }
        if (newVal != null && newVal.length() > 0) {
            Message msg = mHandler.obtainMessage();
            msg.what = EVENT_UPDATE_UI;
            msg.obj = newVal;
            mHandler.sendMessage(msg);
        }
    }

    private String updateAipqValue(String ValStr) {
        if (ValStr == null || ValStr.isEmpty()) return "";
        String[] sciences = ValStr.split(";");
        StringBuilder str = new StringBuilder();
        for (String science : sciences) {
            if (!science.contains(":") && mScene.get(science.trim()) != null) {
                str.append(mScene.get(science.trim()) + ":" + 0 + "%" + "  ");
            } else if (science.contains(":")) {
                String[] par = science.split("\\:");
                if (par.length != 2) continue;
                //Log.d("TAG","par[0]="+par[0]);
                if (par[0].equals(OTHER_SCENE)) {
                    Log.d("TAG", "OTHER_SCENEOTHER_SCENEOTHER_SCENE");
                    continue;
                }
                double val = 0;
                try {
                    val = 1.0 * Double.valueOf(par[1]);
                    val = val / 100;
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    val = 0;
                } finally {
                    if (mScene.get(par[0].trim()) != null) {
                        str.append(mScene.get(par[0].trim()) + ":" + val + "%" + "\n");
                    }
                }

            }

        }
        return str.toString();
    }

    //display top view
    private void showAipqTopView(boolean show) {
        Log.d("V", "mRemoteView.isCreated()" + mRemoteView.isCreated() + "mRemoteView" + mRemoteView);
        if (show) {
            if (!mRemoteView.isCreated()) {
                mRemoteView.createView(getApplicationContext());
            }
            mRemoteView.show();
        } else {
            mRemoteView.hide();
        }
    }

    class ReadingThreadLooper extends Thread {
        private AiPqService service;
        private int threadId = -1;
        private boolean stopFlag;

        ReadingThreadLooper(AiPqService service, int threadId) {
            Log.d("V", "threadId:" + threadId);
            this.threadId = threadId;
            this.service = service;
        }

        @Override
        public void run() {
            while (threadId == service.mThreadId) {
                if (stopFlag) {
                    Log.d("V", "beak threadId:" + threadId + "service.mThreadId" + service.mThreadId);
                    break;
                }
                String scenseVal = mSystemControlManager.readSysFs(SCENE_FS);
                // String scenseVal="6:8523;0:98;5:93;7:0;7:0";
                Log.d("V", "read:" + scenseVal);
                service.updateUI(scenseVal);
                try {
                    sleep(16);
                } catch (InterruptedException e) {
                    stopFlag = true;
                }
            }
            Log.d("V", "Thread" + threadId + " exist" + service.mThreadId);
        }

        public void stopReading() {
            stopFlag = true;
        }
    }

    ;
}
