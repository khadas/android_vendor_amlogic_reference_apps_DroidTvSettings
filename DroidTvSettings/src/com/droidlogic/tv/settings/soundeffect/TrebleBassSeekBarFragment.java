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

package com.droidlogic.tv.settings.soundeffect;

import android.os.Bundle;
import android.os.Handler;
import com.droidlogic.tv.settings.SettingsPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.AudioEffectManager;
import com.droidlogic.tv.settings.TvSettingsActivity;
import com.droidlogic.tv.settings.R;

public class TrebleBassSeekBarFragment extends SettingsPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "TrebleBassSeekBarFragment";

    private SeekBar seekbar_treble;
    private SeekBar seekbar_bass;

    private TextView text_treble;
    private TextView text_bass;

    private AudioEffectManager mAudioEffectManager;
    private boolean isSeekBarInited = false;

    public static TrebleBassSeekBarFragment newInstance() {
        return new TrebleBassSeekBarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mAudioEffectManager == null) {
            mAudioEffectManager = ((TvSettingsActivity)getActivity()).getAudioEffectManager();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.tv_sound_treblebass_seekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    private void initSeekBar(View view) {
        int status = -1;
        boolean hasfocused = false;
        seekbar_treble = (SeekBar) view.findViewById(R.id.seekbar_tv_treble);
        text_treble = (TextView) view.findViewById(R.id.text_tv_treble);
        if (true) {
            status = mAudioEffectManager.getTrebleStatus();
            seekbar_treble.setOnSeekBarChangeListener(this);
            seekbar_treble.setProgress(status);
            setShow(R.id.seekbar_tv_treble, status);
            seekbar_treble.requestFocus();
            hasfocused = true;
        } else {
            seekbar_treble.setVisibility(View.GONE);
            text_treble.setVisibility(View.GONE);
        }
        seekbar_bass = (SeekBar) view.findViewById(R.id.seekbar_tv_bass);
        text_bass = (TextView) view.findViewById(R.id.text_tv_bass);
        if (true) {
            status = mAudioEffectManager.getBassStatus();
            seekbar_bass.setOnSeekBarChangeListener(this);
            seekbar_bass.setProgress(status);
            setShow(R.id.seekbar_tv_bass, status);
            if (!hasfocused) {
                seekbar_bass.requestFocus();
                hasfocused = true;
            }
        } else {
            seekbar_bass.setVisibility(View.GONE);
            text_bass.setVisibility(View.GONE);
        }
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        boolean supportMs12Dap = OutputModeManager.getInstance(getActivity()).isAudioSupportMs12System();
        switch (seekBar.getId()) {
            case R.id.seekbar_tv_treble:{
                setShow(R.id.seekbar_tv_treble, progress);
                mAudioEffectManager.setTreble(progress/* - mTvOptionSettingManager.getTrebleStatus()*/);
                break;
            }
            case R.id.seekbar_tv_bass:{
                setShow(R.id.seekbar_tv_bass, progress);
                mAudioEffectManager.setBass(progress/* - mTvOptionSettingManager.getBassStatus()*/);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    private void setShow(int id, int value) {
        switch (id) {
            case R.id.seekbar_tv_treble:{
                text_treble.setText(getShowString(R.string.tv_treble, value));
                break;
            }
            case R.id.seekbar_tv_bass:{
                text_bass.setText(getShowString(R.string.tv_bass, value));
                break;
            }
            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + ": " + value + "%";
    }
}
