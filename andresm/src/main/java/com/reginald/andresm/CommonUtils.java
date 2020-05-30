package com.reginald.andresm;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class CommonUtils {

    private CommonUtils() {
    }  // Prevent instantiation

    /**
     * Returns a file whose name matches {@code filename}, or null if no file was found.
     *
     * @param apkFile  The file containing the apk zip archive.
     * @param filename The full filename (e.g. res/raw/foo.bar).
     * @return A byte array containing the contents of the matching file, or null if not found.
     * @throws IOException Thrown if there's a matching file, but it cannot be read from the apk.
     */
    public static byte[] getFile(File apkFile, String filename) throws IOException {
        Map<String, byte[]> files = getFiles(apkFile, Pattern.quote(filename));
        return files.get(filename);
    }

    /**
     * Returns all files in an apk.
     *
     * @param apkFile The file containing the apk zip archive.
     * @return A mapping of the matched filenames to their byte contents.
     * @throws IOException Thrown if a matching file cannot be read from the apk.
     */
    public static Map<String, byte[]> getFiles(File apkFile) throws IOException {
        return getFiles(apkFile, (Pattern) null);
    }

    /**
     * Returns all files in an apk that match a given regular expression.
     *
     * @param apkFile The file containing the apk zip archive.
     * @param regex   A regular expression to match the requested filenames.
     * @return A mapping of the matched filenames to their byte contents.
     * @throws IOException Thrown if a matching file cannot be read from the apk.
     */
    public static Map<String, byte[]> getFiles(File apkFile, String regex) throws IOException {
        return getFiles(apkFile, Pattern.compile(regex));
    }

    /**
     * Returns all files in an apk that match a given regular expression.
     *
     * @param apkFile The file containing the apk zip archive.
     * @param regex   A regular expression to match the requested filenames.
     * @return A mapping of the matched filenames to their byte contents.
     * @throws IOException Thrown if a matching file cannot be read from the apk.
     */
    public static Map<String, byte[]> getFiles(File apkFile, Pattern regex) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();  // Retain insertion order
        // Extract apk
        try (ZipFile apkZip = new ZipFile(apkFile)) {
            Enumeration<? extends ZipEntry> zipEntries = apkZip.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                // Visit all files with the given extension
                if (regex == null || regex.matcher(zipEntry.getName()).matches()) {
                    // Map class name to definition
                    try (InputStream is = new BufferedInputStream(apkZip.getInputStream(zipEntry))) {
                        files.put(zipEntry.getName(), IOUtils.toByteArray(is));
                    }
                }
            }
        }
        return files;
    }
}