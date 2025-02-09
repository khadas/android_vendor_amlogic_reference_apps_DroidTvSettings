/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class LongClickPreference extends Preference implements View.OnLongClickListener {

    public interface OnLongClickListener {
        boolean onPreferenceLongClick(Preference preference);
    }

    private OnLongClickListener mLongClickListener;

    public LongClickPreference(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LongClickPreference(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LongClickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongClickPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        return mLongClickListener != null && mLongClickListener.onPreferenceLongClick(this);
    }

    public void setLongClickListener(OnLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }
}
