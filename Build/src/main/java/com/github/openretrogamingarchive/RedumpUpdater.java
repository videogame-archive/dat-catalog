package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.github.openretrogamingarchive.Util.Type;
import static com.github.openretrogamingarchive.Util.downloadBytes;
import static com.github.openretrogamingarchive.Util.downloadToFile;
import static com.github.openretrogamingarchive.Util.saveCSV;
import static com.github.openretrogamingarchive.Util.saveDatCSV;
import static com.github.openretrogamingarchive.Util.scrap;
import static com.github.openretrogamingarchive.Util.scrapOne;

public class RedumpUpdater {

    private static final String ROOT_DIR = "Redump";

    public static void updateRedump(String pathToRoot) throws IOException {
        List<RedumpSystem> systems = getRedumpSystems();
        saveSystemDats(Path.of(pathToRoot, ROOT_DIR), systems);
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

    public static final String DOMAIN = "http://redump.org";
    public static final String DOWNLOADS_URL = DOMAIN + "/downloads/";
    private static List<RedumpSystem> getRedumpSystems() throws IOException {
        String publicRedumpDownloadsPage = new String(downloadBytes(DOWNLOADS_URL), StandardCharsets.UTF_8);
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
                Path redumpSystemDat = downloadToFile(DOMAIN + redumpSystem.getDatDownloadURL(), redumpSystemDir, true);
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
                Path redumpSystemDat = downloadToFile(DOMAIN + redumpSystem.getSubChannelsSBIDatDownloadURL(), redumpSystemDir, false);
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
                Path redumpSystemDat = downloadToFile(DOMAIN + redumpSystem.getBiosDatDownloadURL(), redumpSystemDir, true);
                // Dat Index
                saveDatCSV(redumpSystemDir, redumpSystemDat);
                // Main Index
                mainIndexDirs.add(new String[]{Type.DIRECTORY.name(), redumpSystem.getName() + " - BIOS Images"});
            }
        }
        saveCSV(redumpRoot, mainIndexDirs);
    }

}
