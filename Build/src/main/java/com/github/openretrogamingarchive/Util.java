package com.github.openretrogamingarchive;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

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

    public static byte[] downloadBytes(String url) throws IOException {
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
        return outStream.toByteArray();
    }

    public static Path downloadToFile(String url, Path parent, boolean unZip) throws IOException {
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
        byte[] content = outStream.toByteArray();

        String contentDisposition = conn.getHeaderField("Content-Disposition");
        String fileName = Util.scrapOne(contentDisposition, "\"","\"");

        if (unZip && fileName.endsWith(".zip")) {
            Map<String, byte[]> zipFiles = unZipInMemory(content);
            String uncompressedFileName = zipFiles.keySet().iterator().next();
            Path file = parent.resolve(uncompressedFileName);
            return Files.write(file, zipFiles.get(uncompressedFileName));
        } else {
            Path file = parent.resolve(fileName);
            return Files.write(file, content);
        }
    }

    //
    // Zip Helper Methods
    //

    public static Map<String, byte[]> unZipInMemory(byte[] bytes) throws IOException {
        Map<String, byte[]> inMemory = new HashMap<>();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.ISO_8859_1);
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

    //
    // CSV Generation Helper methods
    //

    public enum Headers { Type, Name };
    public enum Type { FILE, DIRECTORY };

    public static void saveCSV(Path parent, List<String[]> csvRows) throws IOException {
        StringBuilder rootIndex = new StringBuilder();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(Headers.class);
        CSVPrinter csvPrinter = new CSVPrinter(rootIndex, csvFormat);
        csvPrinter.printRecords(csvRows);
        csvPrinter.close();
        Path redumpIndex = parent.resolve("index.csv");
        Files.write(redumpIndex, rootIndex.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void saveDatCSV(Path parent, Path dat) throws IOException {
        String redumpSystemDatName = dat.getName(dat.getNameCount() - 1).toString();
        String[] record = {Type.FILE.name(), redumpSystemDatName };
        List<String[]> records = new ArrayList<>();
        records.add(record);
        saveCSV(parent, records);
    }
}
