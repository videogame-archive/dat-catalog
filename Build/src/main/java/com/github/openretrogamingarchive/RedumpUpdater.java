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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.openretrogamingarchive.Util.*;

public class RedumpUpdater {

    private static final String ROOT_REDUMP_DIR = "Redump";

    public static void updateRedump(String pathToRoot) throws IOException {
        List<RedumpSystem> systems = getRedumpSystems();
        saveSystemDats(Path.of(pathToRoot, ROOT_REDUMP_DIR), systems);
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
        String systemsTable = Util.scrap(publicRedumpDownloadsPage, "<table class=\"statistics\" cellspacing=\"0\">", "</table>").get(0);
        List<String> systems = Util.scrap(systemsTable, "<tr>", "</tr>");
        systems.remove(0);

        List<RedumpSystem> redumpSystems = new ArrayList<>(systems.size());
        for (String system:systems) {
            List<String> systemInfo = Util.scrap(system, "<td>", "</td>");
            String name = systemInfo.get(0);
            String dat = Util.scrapOne(systemInfo.get(2), "<a href=\"", "\">");
            String subDat = Util.scrapOne(systemInfo.get(3), "<a href=\"", "\">");
            String biosDat = Util.scrapOne(systemInfo.get(5), "<a href=\"", "\">");
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

}
