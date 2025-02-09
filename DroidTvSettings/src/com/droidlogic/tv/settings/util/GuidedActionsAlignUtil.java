/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.droidlogic.tv.settings.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.FacetProvider;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.ItemAlignmentFacet;
import androidx.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import androidx.leanback.widget.VerticalGridView;

import com.droidlogic.tv.settings.R;


/**
 * Utilities to align the ActionGridView so that the baseline of the title view matches with
 * the keyline of the fragment.
 */
public class GuidedActionsAlignUtil {

    /**
     * As we want to align to the mean line of the text view, we should always provide a customized
     * viewholder with the new facet when we are creating a GuidedActionStylist.
     */
    private static class SetupViewHolder extends GuidedActionsStylist.ViewHolder implements
            FacetProvider {
        SetupViewHolder(View v) {
            super(v);
        }

        // Provide a customized ItemAlignmentFacet so that the mean line of textView is matched.
        // Here we use mean line of the textview to work as the baseline to be matched with
        // guidance title baseline.
        @Override
        public Object getFacet(Class facet) {
            if (facet.equals(ItemAlignmentFacet.class)) {
                ItemAlignmentFacet.ItemAlignmentDef alignedDef =
                        new ItemAlignmentFacet.ItemAlignmentDef();
                alignedDef.setItemAlignmentViewId(
                        androidx.leanback.R.id.guidedactions_item_title);
                alignedDef.setAlignedToTextViewBaseline(false);
                alignedDef.setItemAlignmentOffset(0);
                alignedDef.setItemAlignmentOffsetWithPadding(true);
                // 50 refers to 50 percent, which refers to mid position of textView.
                alignedDef.setItemAlignmentOffsetPercent(50);
                ItemAlignmentFacet f = new ItemAlignmentFacet();
                f.setAlignmentDefs(new ItemAlignmentDef[]{alignedDef});
                return f;
            }
            return null;
        }
    }

    /**
     * Create a customized GuidedActionsStylist for {@link GuidedStepSupportFragment} used in device
     * name setup.
     */
    public static GuidedActionsStylist createGuidedActionsStylist() {
        return new GuidedActionsStylist() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View v = inflater.inflate(onProvideItemLayoutId(viewType), parent, false);
                return new GuidedActionsAlignUtil.SetupViewHolder(v);
            }
        };
    }

    /**
     * Create a customized GuidedActionsStylist {@link GuidedStepSupportFragment} WITHOUT background
     * used in device name customization input step.
     */
    public static GuidedActionsStylist createNoBackgroundGuidedActionsStylist() {
        return new GuidedActionsStylist() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View v = inflater.inflate(onProvideItemLayoutId(viewType), parent, false);
                v.setBackground(null);
                return new GuidedActionsAlignUtil.SetupViewHolder(v);
            }

            @Override
            public int onProvideItemLayoutId() {
                return R.layout.device_name_input_item;
            }
        };
    }

    /**
     * Create a customized view for {@link GuidedStepSupportFragment} used
     * in device name setup.
     */
    public static View createView(View view, GuidedStepSupportFragment guidedStepFragment) {
        // action_fragment_root's padding cannot be set via attributes so we do it programmatically.
        final View actionFragmentRoot = view.findViewById(R.id.action_fragment_root);
        if (actionFragmentRoot != null) {
            actionFragmentRoot.setPadding(0, 0, 0, 0);
        }

        final VerticalGridView gridView = guidedStepFragment.getGuidedActionsStylist()
                .getActionsGridView();
        gridView.setItemSpacing(
                guidedStepFragment.getResources()
                        .getDimensionPixelSize(R.dimen.setup_list_item_margin));

        // Make the key line match with the item baseline. For our case, the item baseline is
        // customized to be the mean line of title text view.
        gridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_HIGH_EDGE);
        gridView.setWindowAlignmentPreferKeyLineOverHighEdge(true);
        return view;
    }

    /**
     * Create a customized GuidanceStylist for {@link GuidedStepSupportFragment} used in device name
     * setup.
     */
    public static GuidanceStylist createGuidanceStylist() {
        return new GuidanceStylist() {
            @Override
            public int onProvideLayoutId() {
                return R.layout.device_name_content;
            }
        };
    }

}
