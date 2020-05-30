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

class AndroidGradleCompat {

    static File fetchPackageOutputFile(Object task) {
        if (task.properties['resPackageOutputFolder'] != null) {
            return asFile(task.resPackageOutputFolder)
        }

        if (task.properties['packageOutputFile'] != null) {
            return asFile(task.packageOutputFile)
        }

        return null
    }

    static File fetchSourceOutputDirFile(Object task) {
        if (task.properties['sourceOutputDir'] != null) {
            return asFile(task.sourceOutputDir)
        }

        if (task.properties['RClassOutputJar'] != null) {
            return asFile(task.RClassOutputJar)
        }

        return null
    }

    static File fetchTextSymbolOutputDir(Object task) {
        try {
            return asFile(task.getTextSymbolOutputFile())
        } catch (Exception e) {
            e.printStackTrace()
        }

        if (task.properties['textSymbolOutputDir'] != null) {
            return task.textSymbolOutputDir
        }

        return null
    }

    static File asFile(Object input) {
        try {
            return (File) input;
        } catch (Exception e) {
            println(e)
        }

        return input.getAsFile().get();
    }
}
