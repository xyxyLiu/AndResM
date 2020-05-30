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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.gradle.api.GradleException;

import com.reginald.andresm.arsc.Chunk;
import com.reginald.andresm.arsc.ResourceFile;
import com.reginald.andresm.arsc.ResourceTableChunk;
import com.reginald.andresm.arsc.ResourcesHandler;

public class AndResM {

    public static final String DEFAULT_TMP_SUFFIX = ".tmp";
    public static final String DEFAULT_DEBUG_TAG = "andresm";

    private static final String RESOURCES_ARSC = "resources.arsc";
    private static final List<String> RESOURCES_KEEP_DIRS = new ArrayList<>();
    private static final int DEFAULT_PKG_ID = 0x7f;

    private ResourcesHandler mResroucesHandler;
    private static boolean sDebug = false;

    static {
        RESOURCES_KEEP_DIRS.add("res/raw/");
    }

    public AndResM(int targetPkgId, int newPkgId) {
        ResourcesHandler.Config config = new ResourcesHandler.Config(targetPkgId, newPkgId);
        mResroucesHandler = new ResourcesHandler(config);
    }

    public AndResM(int newPkgId) {
        this(DEFAULT_PKG_ID, newPkgId);
    }

    public static void enableDebug(boolean enabled) {
        sDebug = enabled;
    }

    public void replaceAaptOutput(File aaptApk, File sourceOutputDir, File symbolOutputDir) {
        log(String.format("start replacement ... \naaptApkDir = %s, \nsourceOutputDir = %s, \nsymbolOutputDir = %s",
                aaptApk.getAbsolutePath(), sourceOutputDir.getAbsolutePath(), symbolOutputDir.getAbsolutePath()));

        if (aaptApk == null || !aaptApk.exists()) {
            throw new GradleException("resource apk NOT found!");
        }

        if (sourceOutputDir == null || !sourceOutputDir.exists()) {
            throw new GradleException("source output NOT found!");
        }

        replaceApkDir(aaptApk);
        replaceR(sourceOutputDir);

        if (symbolOutputDir != null && symbolOutputDir.exists()) {
            replaceR(symbolOutputDir);
        } else {
            log("symbol output NOT found!");
        }
    }

    private void replaceApkDir(File apkFile) {
        log("transform " + apkFile.getAbsolutePath() + " ...");

        if (apkFile.isDirectory()) {
            for (File file : apkFile.listFiles()) {
                replaceApkDir(file);
            }
        } else {
            if (apkFile.getName().contains(".ap_")) {
                try {
                    replaceApk(apkFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * replace resources.ap_
     *
     * @param apk
     */
    private void replaceApk(File apk) {
        log("transform ap_ file " + apk.getAbsolutePath() + " ...");
        ZipOutputStream out = null;
        try {
            Map<String, byte[]> entries = CommonUtils.getFiles(apk);
            File newZip = new File(apk.getAbsolutePath() + DEFAULT_TMP_SUFFIX);
            out = new ZipOutputStream(new FileOutputStream(newZip));
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                String name = entry.getKey();
                byte[] bytes = entry.getValue();

                // handle arsc file
                if (name.equals(RESOURCES_ARSC)) {
                    log("transform arsc " + name + " ....");
                    ResourceFile arscFile = new ResourceFile(bytes);
                    ResourceFile newArsc = transformChunkFile(arscFile);
                    // output a human-readable arsc dump file for mDebug
                    logResourceFile(newArsc, new File(apk.getParentFile(), DEFAULT_DEBUG_TAG + "_arsc.txt"));
                    bytes = newArsc.toByteArray();
                } else if (name.endsWith(".xml")) {
                    // handle xml file.   e.g. AndroidManifest.xml,layout.xml...
                    ResourceFile xmlFile = null;
                    log("transform " + name + " ....");

                    if (isFileKeeped(name, RESOURCES_KEEP_DIRS)) {
                        log("IGNORE xml file: " + name + " due to keep policy:  " + RESOURCES_KEEP_DIRS);
                    } else {
                        xmlFile = new ResourceFile(bytes);
                    }

                    if (xmlFile != null) {
                        ResourceFile newXmlArsc = transformChunkFile(xmlFile);
                        bytes = newXmlArsc.toByteArray();
                    }
                }

                ZipEntry e = new ZipEntry(name);
                out.putNextEntry(e);
                out.write(bytes);
                out.closeEntry();
            }

            out.close();

            apk.delete();
            newZip.renameTo(apk);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * replace all 0x7f -> 0xPP in R.java or R.txt
     *
     * @param rFile
     */
    private void replaceR(File rFile) {
        log("transform " + rFile.getAbsolutePath() + " ...");
        if (rFile.isDirectory()) {
            for (File file : rFile.listFiles()) {
                replaceR(file);
            }
        } else {
            String fileName = rFile.getName();
            if (fileName.startsWith("R.")) {
                if (fileName.endsWith(".java") || fileName.endsWith(".txt")) {
                    mResroucesHandler.handleRFile(rFile);
                } else if (fileName.endsWith(".jar")) {
                    mResroucesHandler.handleRJar(rFile);
                }
            }
        }
    }

    /**
     * replace ids in a chunk file
     *
     * @param arsc
     *
     * @return
     */
    private ResourceFile transformChunkFile(ResourceFile arsc) {
        try {
            mResroucesHandler.handleResourceFile(arsc);
            return new ResourceFile(arsc.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isFileKeeped(String fileName, List<String> keepPrefixs) {
        for (String keepPrefix : keepPrefixs) {
            if (fileName.startsWith(keepPrefix)) {
                return true;
            }
        }
        return false;
    }

    private void logResourceFile(ResourceFile resourceFile, File logFile) {
        if (sDebug) {
            log("output resource dump file to " + logFile.getAbsolutePath());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(logFile);
                List<Chunk> chunks = resourceFile.getChunks();
                for (Chunk chunk : chunks) {
                    fos.write(chunk.toArscString().getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printArsc(ResourceFile resourceFile) {
        List<Chunk> chunks = resourceFile.getChunks();
        Chunk firstTable = chunks.get(0);
        if (firstTable instanceof ResourceTableChunk) {
            ResourceTableChunk resourceChunk = (ResourceTableChunk) firstTable;
            log(resourceChunk.toArscString());
        }
    }

    private void printXml(ResourceFile resourceFile) {
        List<Chunk> xmlChunks = resourceFile.getChunks();
        for (Chunk xc : xmlChunks) {
            log(xc.toArscString());
        }
    }

    public static void log(String text) {
        if (sDebug) {
            System.out.println(String.format("[%s] %s", "AndResM", text));
        }
    }

}
