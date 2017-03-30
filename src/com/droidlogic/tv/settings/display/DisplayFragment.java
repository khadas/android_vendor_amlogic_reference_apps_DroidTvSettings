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

package com.droidlogic.tv.settings.display;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.droidlogic.tv.settings.util.DroidUtils;
import com.droidlogic.tv.settings.R;

public class DisplayFragment extends LeanbackPreferenceFragment {

	private static final String TAG = "DisplayFragment";

	private static final String KEY_OUTPUTMODE = "outputmode";
	private static final String KEY_HDR = "hdr";
	private static final String KEY_SDR = "sdr";

	private boolean mTvUiMode;

	public static DisplayFragment newInstance() {
		return new DisplayFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.display, null);
		mTvUiMode = DroidUtils.hasTvUiMode();

		final Preference outputmodePref = findPreference(KEY_OUTPUTMODE);
		DroidUtils.invisiblePreference(outputmodePref, mTvUiMode);

		final Preference sdrPref = findPreference(KEY_SDR);
		DroidUtils.invisiblePreference(sdrPref, mTvUiMode);

		final Preference hdrPref = findPreference(KEY_HDR);
		DroidUtils.invisiblePreference(hdrPref, mTvUiMode);
	}
}
