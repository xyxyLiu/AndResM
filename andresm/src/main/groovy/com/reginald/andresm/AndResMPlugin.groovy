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
package com.reginald.andresm

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.reginald.andresm.utils.AndroidGradleCompat

class AndResMPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("andresm", AndResMExtension)
        AndResMExtension extension = project.andresm

        project.afterEvaluate {
            project.android.applicationVariants.all { variant ->
                variant.outputs.each { variantOutput ->
                    // hook process resource task
                    def processResources = variantOutput.getProcessResources()
                    processResources.doLast {
                        println "android gradle plugin " + AndroidGradleCompat.getAndroidGradlePluginVersion()
                        println "process AndResM: 0x7f -> 0x" + Integer.toHexString(extension.packageId) + " ... "
                        AndResM andResM = new AndResM(extension.packageId)
                        andResM.enableDebug(extension.debug)
                        File packageOutputFile = AndroidGradleCompat.fetchPackageOutputFile(processResources)
                        File sourceOutputDir = AndroidGradleCompat.fetchSourceOutputDirFile(processResources)
                        File textSymbolOutputDir = AndroidGradleCompat.fetchTextSymbolOutputDir(processResources)

                        andResM.replaceAaptOutput(packageOutputFile, sourceOutputDir, textSymbolOutputDir)
                    }
                }
            }
        }
    }

}