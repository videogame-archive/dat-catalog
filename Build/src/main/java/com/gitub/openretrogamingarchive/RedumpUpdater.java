package com.gitub.openretrogamingarchive;

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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RedumpUpdater {

    private static final String ROOT_LATEST_DIR = "../latest";

    private static final String ROOT_REDUMP_DIR = "Redump";

    public static void updateRedump() throws IOException {
        List<RedumpSystem> systems = getRedumpSystems();
        saveSystemDats(Path.of(ROOT_LATEST_DIR, ROOT_REDUMP_DIR), systems);
    }

    public static class RedumpSystem {
        private final String name;
        private final String datDownloadURL;
        private final String subChannelsSBIDatDownloadURL;
        private final String biosDatDownloadURL;

        public RedumpSystem(String name, String datDownloadURL, String subChannelsSBIDatDownloadURL, String biosDatDownloadURL) {
            this.name = name;
            this.datDownloadURL = datDownloadURL;
            this.subChannelsSBIDatDownloadURL = subChannelsSBIDatDownloadURL;
            this.biosDatDownloadURL = biosDatDownloadURL;
        }

        public String getName() {
            return name;
        }

        public String getDatDownloadURL() {
            return datDownloadURL;
        }

        public String getSubChannelsSBIDatDownloadURL() {
            return subChannelsSBIDatDownloadURL;
        }

        public String getBiosDatDownloadURL() {
            return biosDatDownloadURL;
        }
    }

    public static final String REDUMP_DOMAIN = "http://redump.org";
    public static final String REDUMP_DOWNLOADS_URL = REDUMP_DOMAIN + "/downloads/";
    private static List<RedumpSystem> getRedumpSystems() throws IOException {
        String publicRedumpDownloadsPage = new String(downloadBytes(REDUMP_DOWNLOADS_URL), StandardCharsets.UTF_8);
        String systemsTable = scrap(publicRedumpDownloadsPage, "<table class=\"statistics\" cellspacing=\"0\">", "</table>").get(0);
        List<String> systems = scrap(systemsTable, "<tr>", "</tr>");
        systems.remove(0);

        List<RedumpSystem> redumpSystems = new ArrayList<>(systems.size());
        for (String system:systems) {
            List<String> systemInfo = scrap(system, "<td>", "</td>");
            String name = systemInfo.get(0);
            String dat = scrapOne(systemInfo.get(2), "<a href=\"", "\">");
            String subDat = scrapOne(systemInfo.get(3), "<a href=\"", "\">");
            String biosDat = scrapOne(systemInfo.get(5), "<a href=\"", "\">");
            redumpSystems.add(new RedumpSystem(name, dat, subDat, biosDat));
        }
        return redumpSystems;
    }

    private static void saveSystemDats(Path redumpRoot, List<RedumpSystem> redumpSystems) throws IOException {
        // Main index
        List<String[]> mainIndexDirs = new ArrayList<>();

        //
        if (!Files.exists(redumpRoot)) {
            Files.createDirectories(redumpRoot);
        }

        for (RedumpSystem redumpSystem: redumpSystems) {
            if (redumpSystem.getDatDownloadURL() != null) {
                Path redumpSystemDir = redumpRoot.resolve(redumpSystem.getName());
                if (!Files.exists(redumpSystemDir)) {
                    Files.createDirectory(redumpSystemDir);
                }
                Path redumpSystemDat = downloadToFile(REDUMP_DOMAIN + redumpSystem.getDatDownloadURL(), redumpSystemDir, true);
                // Dat Index
                saveDatCSV(redumpSystemDir, redumpSystemDat);
                // Main Index
                mainIndexDirs.add(new String[]{Type.DIRECTORY.name(), redumpSystem.getName()});
            }

            if (redumpSystem.getSubChannelsSBIDatDownloadURL() != null) {
                Path redumpSystemDir = redumpRoot.resolve(redumpSystem.getName() + " - SBI Subchannels");
                if (!Files.exists(redumpSystemDir)) {
                    Files.createDirectory(redumpSystemDir);
                }
                Path redumpSystemDat = downloadToFile(REDUMP_DOMAIN + redumpSystem.getSubChannelsSBIDatDownloadURL(), redumpSystemDir, false);
                // Dat Index
                saveDatCSV(redumpSystemDir, redumpSystemDat);
                // Main Index
                mainIndexDirs.add(new String[]{Type.DIRECTORY.name(), redumpSystem.getName() + " - SBI Subchannels"});
            }

            if (redumpSystem.getBiosDatDownloadURL() != null) {
                Path redumpSystemDir = redumpRoot.resolve(redumpSystem.getName() + " - BIOS Images");
                if (!Files.exists(redumpSystemDir)) {
                    Files.createDirectory(redumpSystemDir);
                }
                Path redumpSystemDat = downloadToFile(REDUMP_DOMAIN + redumpSystem.getBiosDatDownloadURL(), redumpSystemDir, true);
                // Dat Index
                saveDatCSV(redumpSystemDir, redumpSystemDat);
                // Main Index
                mainIndexDirs.add(new String[]{Type.DIRECTORY.name(), redumpSystem.getName() + " - BIOS Images"});
            }
        }
        saveCSV(redumpRoot, mainIndexDirs);
    }

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

    private static byte[] downloadBytes(String url) throws IOException {
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

    private static Path downloadToFile(String url, Path parent, boolean unZip) throws IOException {
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
        String fileName = scrapOne(contentDisposition, "\"","\"");

        if (unZip && fileName.endsWith(".zip")) {
            return unZipFirstFile(parent, content);
        } else {
            Path file = parent.resolve(fileName);
            return Files.write(file, content);
        }
    }

    private static Path unZipFirstFile(Path parent, byte[] bytes) throws IOException {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry zipEntry = zis.getNextEntry();
        String fileName = zipEntry.getName();
        byte[] content = zis.readNBytes(((Long) zipEntry.getSize()).intValue());
        Path file = parent.resolve(fileName);
        zis.closeEntry();
        zis.close();
        return Files.write(file, content);
    }

    //
    // CSV Generation Helper methods
    //

    private enum Headers { Type, Name, URL };
    private enum Type { FILE, DIRECTORY };

    private static void saveCSV(Path redumpRoot, List<String[]> csvRows) throws IOException {
        StringBuilder rootIndex = new StringBuilder();
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(Headers.class);
        CSVPrinter csvPrinter = new CSVPrinter(rootIndex, csvFormat);
        csvPrinter.printRecords(csvRows);
        csvPrinter.close();
        Path redumpIndex = redumpRoot.resolve("index.csv");
        Files.write(redumpIndex, rootIndex.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static final String DOWNLOAD_URL_TEMPLATE = "https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/latest/Redump/";
    private static void saveDatCSV(Path redumpSystemDir, Path redumpSystemDat) throws IOException {
        String redumpSystemDirName = redumpSystemDir.getName(redumpSystemDir.getNameCount() - 1).toString();
        String redumpSystemDatName = redumpSystemDat.getName(redumpSystemDat.getNameCount() - 1).toString();
        String[] record = {Type.FILE.name(), redumpSystemDatName, DOWNLOAD_URL_TEMPLATE + redumpSystemDirName + "/" + redumpSystemDatName};
        List<String[]> records = new ArrayList<>();
        records.add(record);
        saveCSV(redumpSystemDir, records);
    }
}
