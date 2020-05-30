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
package com.reginald.andresm;

public class AndResMExtension {
    private boolean debug = false;
    private int targetPackageId = 0x7f;
    private int packageId = 0x7f;

    public int getTargetPackageId() {
        return targetPackageId;
    }

    public void setTargetPackageId(int id) {
        targetPackageId = id;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int id) {
        packageId = id;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public boolean isDebug() {
        return debug;
    }
}
