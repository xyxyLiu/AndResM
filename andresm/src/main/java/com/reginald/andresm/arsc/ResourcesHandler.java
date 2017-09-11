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
package com.reginald.andresm.arsc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A handler to re-config all package id.
 */
public class ResourcesHandler {
    public static final String DEFAULT_TMP_SUFFIX = ".tmp";

    public static class Config {
        public final int oldId;
        public final int packageId;

        public Config(int oldId, int pkgId) {
            this.oldId = oldId;
            this.packageId = pkgId;
        }
    }

    private Config mConfig;

    public ResourcesHandler(Config config) {
        mConfig = config;
    }

    public void handleResourceFile(ResourceFile resourceFile) {
        List<Chunk> chunks = resourceFile.getChunks();
        for (Chunk chunk : chunks) {
            handleChunk(chunk);
        }
    }

    /**
     * replace all 0x7f -> 0xPP in R.java or R.txt
     * @param file
     */
    public void handleRFile(File file) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            File newFile = new File(file.getAbsolutePath() + DEFAULT_TMP_SUFFIX);
            if (newFile.exists()) {
                newFile.delete();
            }

            Pattern pattern = Pattern.compile(String.format("0x%02x[0-9a-fA-F]{6}", mConfig.oldId));

            writer = new BufferedWriter(new FileWriter(newFile));
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String id = matcher.group();
                    matcher.appendReplacement(sb, String.format("0x%02x%s", mConfig.packageId, id.substring(4)));
                }
                matcher.appendTail(sb);

                writer.write(sb.toString());
                writer.newLine();
            }

            writer.flush();
            writer.close();
            reader.close();

            file.delete();
            newFile.renameTo(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * There are all together 4 target types of chunk:
     * 1. for arsc file:
     * 1.1 PackageChunk: replace package id in header, add Library Chunk for dynamic package mapping.
     * 1.2 TypeChunk: replace all entries in each TypeChunk
     * 2. for xml file:
     * 2.1 XmlStartElementChunk: replace all attributes
     * 2.2 XmlResourceMapChunk: replace resource id map
     *
     * @param chunk
     */
    private void handleChunk(Chunk chunk) {
        if (chunk instanceof PackageChunk) {
            PackageChunk packageChunk = (PackageChunk) chunk;
            // replace id in PackageChunk
            if (packageChunk.getId() == mConfig.oldId) {
                packageChunk.setId(mConfig.packageId);
            }
            Map<Integer, String> pkgMap = new LinkedHashMap<>();
            pkgMap.put(mConfig.packageId, packageChunk.getPackageName());
            packageChunk.addChunk(0, LibraryChunk.create(packageChunk, pkgMap));
        } else if (chunk instanceof TypeChunk) {
            TypeChunk typeChunk = (TypeChunk) chunk;
            TreeMap<Integer, TypeChunk.Entry> newEntryMap = new TreeMap<>();
            for (Map.Entry<Integer, TypeChunk.Entry> mapEntry : typeChunk.getEntries().entrySet()) {
                newEntryMap.put(mapEntry.getKey(), reconfigTypeChunkEntry(mapEntry.getValue()));
            }
            typeChunk.setEntries(newEntryMap);
        } else if (chunk instanceof XmlStartElementChunk) {
            XmlStartElementChunk xmlStartElementChunk = (XmlStartElementChunk) chunk;
            List<XmlAttribute> newAttributes = new ArrayList<>();
            for (XmlAttribute attribute : xmlStartElementChunk.getAttributes()) {
                newAttributes.add(new AutoValue_XmlAttribute(attribute.namespaceIndex(),
                        attribute.nameIndex(), attribute.rawValueIndex(), reconfigResourceValue(attribute.typedValue()),
                        attribute.parent()));
            }
            xmlStartElementChunk.setAttributes(newAttributes);
        } else if (chunk instanceof XmlResourceMapChunk) {
            XmlResourceMapChunk xmlResourceMapChunk = (XmlResourceMapChunk) chunk;
            List<Integer> newResourceIds = new ArrayList<>();
            for (Integer id : xmlResourceMapChunk.getResources()) {
                newResourceIds.add(reconfigPkgId(id));
            }
            xmlResourceMapChunk.setResources(newResourceIds);
        }

        // handle sub-chunks
        if (chunk instanceof ChunkWithChunks) {
            ChunkWithChunks chunkWithChunks = (ChunkWithChunks) chunk;
            for (Chunk c : chunkWithChunks.getChunks()) {
                handleChunk(c);
            }
        }
    }

    private TypeChunk.Entry reconfigTypeChunkEntry(TypeChunk.Entry typeEntry) {

        if (typeEntry.isComplex()) {
            Map<Integer, ResourceValue> reconfigResourceValueMap = new LinkedHashMap<>();
            Iterator<Map.Entry<Integer, ResourceValue>> iterator = typeEntry.values().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, ResourceValue> mapEntry = iterator.next();
                reconfigResourceValueMap.put(reconfigPkgId(mapEntry.getKey()), reconfigResourceValue(mapEntry.getValue()));
            }
            return new AutoValue_TypeChunk_Entry(
                    typeEntry.headerSize(), typeEntry.flags(), typeEntry.keyIndex(),
                    typeEntry.value(), reconfigResourceValueMap,
                    reconfigPkgId(typeEntry.parentEntry()), typeEntry.parent());
        } else {
            return new AutoValue_TypeChunk_Entry(
                    typeEntry.headerSize(), typeEntry.flags(), typeEntry.keyIndex(),
                    reconfigResourceValue(typeEntry.value()), typeEntry.values(),
                    typeEntry.parentEntry(), typeEntry.parent());
        }
    }

    private ResourceValue reconfigResourceValue(ResourceValue resourceValue) {
        if (resourceValue.type() == ResourceValue.Type.REFERENCE || resourceValue.type() == ResourceValue.Type.ATTRIBUTE ||
                resourceValue.type() == ResourceValue.Type.DYNAMIC_REFERENCE) {
            return new AutoValue_ResourceValue(resourceValue.size(), resourceValue.type(), reconfigPkgId(resourceValue.data()));
        }
        return resourceValue;
    }

    private int reconfigPkgId(int pkgId) {
        if (needReconfigPkgId(pkgId)) {
            return (mConfig.packageId << 24) | (pkgId & 0x00ffffff);
        }
        return pkgId;
    }

    private boolean needReconfigPkgId(int pkgId) {
        return mConfig.oldId == (pkgId >> 24);
    }

}
