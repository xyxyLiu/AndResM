package com.reginald.andresm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static com.reginald.andresm.AndResM.DEFAULT_TMP_SUFFIX;

public class JarModifier {
    private Callback mCallback;

    public void update(File jarFile, Callback callback) throws IOException {
        mCallback = callback;
        File originFile = new File(jarFile.getParent(), "original." + jarFile.getName());
        File updateFile = new File(jarFile.getParent(), jarFile.getName() + DEFAULT_TMP_SUFFIX);
        FileUtils.deleteQuietly(originFile);
        FileUtils.moveFile(jarFile, originFile);
        FileUtils.deleteQuietly(jarFile);

        if (!originFile.exists()) {
            throw new IllegalStateException("no jar file found!");
        }

        final Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
        final JarFile jar = new JarFile(originFile);

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(updateFile));

        try {
            Enumeration<JarEntry> iterator = jar.entries();
            while (iterator.hasMoreElements()) {
                JarEntry jarEntry = iterator.nextElement();
                String entryName = jarEntry.getName();
                JarEntry newJarEntry = new JarEntry(entryName);
                jos.putNextEntry(newJarEntry);
                try {
                    ClassWriter classWriter = updateClassEntry(jar, jarEntry, classes);
                    if (classWriter != null) {
                        log(String.format("update() update entry %s", entryName));
                        jos.write(classWriter.toByteArray());
                    } else {
                        log(String.format("update() keep entry %s", entryName));
                        jos.write(IOUtils.toByteArray(jar.getInputStream(jarEntry)));
                    }
                } finally {
                    jos.closeEntry();
                }
            }
        } finally {
            IOUtils.closeQuietly(jar);
            IOUtils.closeQuietly(jos);
        }

        // replace
        File replaceFile = jarFile;
        FileUtils.moveFile(updateFile, replaceFile);
        if (!replaceFile.exists()) {
            throw new IllegalStateException("jar file replace error!");
        }
    }

    private ClassWriter updateClassEntry(JarFile jar, JarEntry entry, Map<String, ClassNode> classes) throws IOException {
        String name = entry.getName();
        InputStream jis = jar.getInputStream(entry);
        try {
            if (name.endsWith(".class")) {
                byte[] bytes = IOUtils.toByteArray(jis);
                ClassNode cn = readNode(bytes);
                classes.put(cn.name, cn);

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                // modify field: static final members
                for (FieldNode fieldNode : cn.fields) {
                    String fieldName = fieldNode.name;
                    Object fieldValue = fieldNode.value;
                    if (fieldValue instanceof Integer) {
                        int resId = ((Integer) fieldValue).intValue();
                        int newPkgId = mCallback.updatePkgId(resId);
                        fieldNode.value = newPkgId;
                        if (newPkgId != resId) {
                            log(String.format("updateClassEntry() update field %s: %s -> %s",
                                    fieldName, resId, newPkgId));
                        }
                    }
                }

                // modify clinit: static members & arrays
                List<MethodNode> methodList = cn.methods;
                for (MethodNode md : methodList) {
                    if (md.name.equals("<clinit>")) {
                        Iterator<AbstractInsnNode> instructionIter = md.instructions.iterator();
                        while (instructionIter.hasNext()) {
                            AbstractInsnNode instruction = instructionIter.next();
                            if (instruction instanceof LdcInsnNode) {
                                LdcInsnNode ldcInstruction = (LdcInsnNode) instruction;
                                Object value = ldcInstruction.cst;
                                if (value instanceof Integer) {
                                    int resId = ((Integer) value).intValue();
                                    int newPkgId = mCallback.updatePkgId(resId);
                                    ldcInstruction.cst = newPkgId;
                                    if (newPkgId != resId) {
                                        log(String.format("updateClassEntry() update variable: %s -> %s",
                                                resId, newPkgId));
                                    }
                                }
                            }
                        }
                    }
                }

                cn.accept(cw);
                return cw;
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }

        return null;
    }

    private ClassNode readNode(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);
        return cn;
    }

    private void log(String text) {
        AndResM.log(text);
    }

    public interface Callback {
        int updatePkgId(int id);
    }
}
