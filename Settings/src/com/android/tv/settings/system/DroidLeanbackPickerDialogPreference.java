/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.tv.settings.system;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.DialogPreference;

import com.android.tv.settings.R;


/**
 * A {@link DialogPreference} used inside {@link DateTimeFragment} for setting date or time. When
 * clicked, this preference opens a dialog enabling the user to set the date or time.
 */
public class DroidLeanbackPickerDialogPreference extends DialogPreference {

    // the picker preference type: can be either 'date' or 'time'
    private final String mPreferenceType;

    public DroidLeanbackPickerDialogPreference(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LeanbackPickerDialogPreference, 0, 0);
        mPreferenceType = a.getString(R.styleable.LeanbackPickerDialogPreference_pickerType);

        a.recycle();
    }

    public DroidLeanbackPickerDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DroidLeanbackPickerDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    public DroidLeanbackPickerDialogPreference(Context context) {
        this(context, null);
    }

    public String getType() {
        return mPreferenceType;
    }
}
