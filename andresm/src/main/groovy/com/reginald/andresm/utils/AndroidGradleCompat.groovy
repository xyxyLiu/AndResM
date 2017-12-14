/*
 * Copyright 2018 xyxyLiu All Rights Reserved.
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
package com.reginald.andresm.utils

import com.android.builder.model.Version

class AndroidGradleCompat {

    static String getAndroidGradlePluginVersion() {
        return Version.ANDROID_GRADLE_PLUGIN_VERSION
    }

    static File fetchPackageOutputFile(Object task) {
        if (getAndroidGradlePluginVersion().compareTo("3.0") >= 0) {
            return task.resPackageOutputFolder
        } else if (task.properties['packageOutputFile'] != null) {
            return task.packageOutputFile
        }

        return null
    }

    static File fetchSourceOutputDirFile(Object task) {
        if (task.properties['sourceOutputDir'] != null) {
            return task.sourceOutputDir
        }
        return null
    }

    static File fetchTextSymbolOutputDir(Object task) {
        if (getAndroidGradlePluginVersion().compareTo("3.0") >= 0) {
            return task.getTextSymbolOutputFile()
        } else if (task.properties['textSymbolOutputDir'] != null) {
            return task.textSymbolOutputDir
        }
        return null
    }
}
