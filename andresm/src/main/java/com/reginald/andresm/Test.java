package com.reginald.andresm;

import com.reginald.andresm.arsc.Chunk;
import com.reginald.andresm.arsc.ResourceFile;
import com.reginald.andresm.arsc.ResourceTableChunk;
import com.reginald.andresm.arsc.ResourcesHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Test {

    private static final String RESOURCES_ARSC = "resources.arsc";

    public static void test(File apk) {
        try {
            byte[] resourceBytes = CommonUtils.getFile(apk, RESOURCES_ARSC);
            if (resourceBytes == null) {
                throw new IOException(String.format("Unable to find %s in APK.", RESOURCES_ARSC));
            }
            ResourceFile arscFile = new ResourceFile(resourceBytes);
            printArsc(arscFile);

            System.out.println("\n\n transform Arsc ....  \n\n");

            ResourceFile newArsc = transformArsc(arscFile);
            printArsc(newArsc);

            Map<String, byte[]> xmls = CommonUtils.getFiles(apk, ".*\\.xml");
            for (Map.Entry<String, byte[]> xml : xmls.entrySet()) {
                System.out.println(xml.getKey() + ": ");
                ResourceFile xmlFile = new ResourceFile(xml.getValue());
                printXml(xmlFile);

                ResourceFile newXmlArsc = transformArsc(xmlFile);

                System.out.println("\n\n transform " + xml.getKey() + " ....  \n\n");

                printXml(newXmlArsc);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static ResourceFile transformArsc(ResourceFile arsc) {
        try {
            ResourcesHandler chunkHandler = new ResourcesHandler(new ResourcesHandler.Config(0x7f, 0x66));
            chunkHandler.handleRes(arsc);
            return new ResourceFile(arsc.toByteArray());
//            List<Chunk> chunks = arsc.getChunks();
//            Chunk firstTable = chunks.get(0);
//            if (firstTable instanceof  ResourceTableChunk) {
//                ResourceTableChunk resourceChunk = (ResourceTableChunk) firstTable;
//                Map<Integer, String> pkgMap = new LinkedHashMap<>();
//                pkgMap.put(0x7e, "com.test.arsc");
//                System.out.println("length of com.test.arsc: " + "com.test.arsc".getBytes(Charset.forName("UTF-16LE")).length);
//                resourceChunk.addChunk(0, LibraryChunk.create(resourceChunk, pkgMap));
//                return new ResourceFile(arsc.toByteArray());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void printArsc(ResourceFile resourceFile) {

        List<Chunk> chunks = resourceFile.getChunks();
        Chunk firstTable = chunks.get(0);
        if (firstTable instanceof  ResourceTableChunk) {
            ResourceTableChunk resourceChunk = (ResourceTableChunk) firstTable;
            System.out.println(resourceChunk.toArscString());
        }

    }

    private static void printXml(ResourceFile resourceFile) {
        List<Chunk> xmlChunks = resourceFile.getChunks();
        for (Chunk xc : xmlChunks) {
            System.out.println(xc.toArscString());
        }
    }

}
