package com.reginald.andresm.arsc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxy on 17-9-8.
 */

public class ResourcesHandler {

    public static final int DEFAULT_PKG_ID = 0x7f;

    public static class Config {
        public final int oldId;
        public final int packageId;

        public Config(int oldId, int pkgId) {
            this.oldId = oldId;
            this.packageId = pkgId;
        }

        public Config(int pkgId) {
            this(DEFAULT_PKG_ID, pkgId);
        }
    }

    private Config mConfig;

    public ResourcesHandler(Config config) {
        mConfig = config;
    }

    public void handleRes(ResourceFile resourceFile) {
        List<Chunk> chunks = resourceFile.getChunks();
        for (Chunk chunk : chunks) {
            handleChunk(chunk);
        }
    }

    private void handleChunk(Chunk chunk) {
        System.out.println("handleChunk() chunk = " + chunk);
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
            for (TypeChunk.Entry typeEntry : typeChunk.getEntries().values()) {
                reconfigTypeChunkEntry(typeEntry);
            }
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
        System.out.println(String.format("needReconfigPkgId pkgId = 0x%08x, %b", pkgId , (mConfig.oldId == (pkgId >> 24))));
        return mConfig.oldId == (pkgId >> 24);
    }

}
