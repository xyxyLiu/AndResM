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
import org.gradle.api.tasks.compile.JavaCompile

class AndResMPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("andresm", AndResMExtension)
        AndResMExtension extension = project.andresm


        project.afterEvaluate {

            println "apply start 1 ... " + new Date().toLocaleString();

            final int customPackageId = extension.packageId
            project.android.applicationVariants.all { variant ->
                variant.outputs.each { variantOutput ->
                    String fullName = variant.getName().capitalize();

                    println "variant = " + fullName;

                    // hook process resource task
                    def processResources = variantOutput.getProcessResources();
                    processResources.doLast {
                        println "processResources... manifest = " + processResources.manifestFile
                        println "processResources... packageOutputFile = " + processResources.packageOutputFile
                        println "processResources... sourceOutputDir = " + processResources.sourceOutputDir

                        Test.test(processResources.packageOutputFile);
                    }

                    // hook process java compile task
                    JavaCompile javaCompile = variant.getJavaCompile()
                    javaCompile.doFirst {
                        for (File f : javaCompile.source.files) {
                            println "check source input " + f.getAbsolutePath()
                        }
                    }
                }
            }

        }
    }
}