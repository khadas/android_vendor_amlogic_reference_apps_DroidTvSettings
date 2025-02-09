/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.droidlogic.tv.settings.sliceprovider.accessories;

import android.content.ComponentName;
import android.content.ServiceConnection;

/** A thin layer of ServiceConnection that makes cleaning up slightly cleaner. */
public abstract class SimplifiedConnection implements ServiceConnection {

    protected abstract void cleanUp();

    @Override
    public void onServiceDisconnected(ComponentName name) {
        cleanUp();
    }

    @Override
    public void onBindingDied(ComponentName name) {
        cleanUp();
    }
}
