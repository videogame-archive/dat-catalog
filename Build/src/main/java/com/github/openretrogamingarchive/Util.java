package com.github.openretrogamingarchive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Util {
    //
    // Text Processing Helper Methods
    //

    public static String scrapOne(String text, String start, String end) {
        List<String> results = scrap(text, start, end);
        if (results.size() == 1) {
            return results.get(0);
        }
        return null;
    }

    public static List<String> scrap(String text, String start, String end) {
        List<String> results = new ArrayList<>();
        int from = 0;
        while (from < text.length()) {
            int indexOfStart = text.indexOf(start, from);
            int indexOfEnd = text.indexOf(end, indexOfStart + start.length());
            if (indexOfStart != -1 && indexOfEnd != -1) {
                results.add(text.substring(indexOfStart + start.length(), indexOfEnd));
                from = indexOfEnd + end.length();
            } else {
                break;
            }
        }
        return results;
    }

    //
    // Download Helper Methods
    //

    public static class Download {
        private String name;
        private byte[] bytes;

        public Download(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        public String getName() {
            return name;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }

    public static Download download(String url) throws IOException {
        URL URL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
        conn.setRequestMethod("GET");
        InputStream inputStream = conn.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 50];
        int read = -1;
        while ( (read = inputStream.read(bytes)) != -1) {
            outStream.write(bytes, 0, read);
        }
        outStream.close();
        byte[] allBytes = outStream.toByteArray();

        String contentDisposition = conn.getHeaderField("Content-Disposition");
        String name = null;
        if (contentDisposition != null) {
            name = Util.scrapOne(contentDisposition, "\"","\"");
        }

        return new Download(name, allBytes);
    }

    public static Path downloadToFolder(String url, Path parent, boolean unZip) throws IOException {
        Download download = download(url);
        if (unZip && download.getName().endsWith(".zip")) {
            Map<String, byte[]> zipFiles = unZipInMemory(download.getBytes());
            for (String uncompressedFileName:zipFiles.keySet()) {
                Path file = parent.resolve(uncompressedFileName);
                Files.write(file, zipFiles.get(uncompressedFileName));
            }
            return parent;
        } else {
            Path file = parent.resolve(download.getName());
            return Files.write(file, download.getBytes());
        }
    }

    //
    // Zip Helper Methods
    //

    public static Map<String, byte[]> unZipInMemory(byte[] bytes) throws IOException {
        Map<String, byte[]> inMemory = new HashMap<>();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.ISO_8859_1); // StandardCharsets.UTF_8 breaks TOSEC
        ZipEntry zipEntry = null;
        while ((zipEntry = zis.getNextEntry()) != null) {
            if (!zipEntry.isDirectory()) {
                String path = zipEntry.getName();
                byte[] content = zis.readNBytes(((Long) zipEntry.getSize()).intValue());
                inMemory.put(path, content);
            }
        }
        zis.closeEntry();
        zis.close();
        return inMemory;
    }
}
