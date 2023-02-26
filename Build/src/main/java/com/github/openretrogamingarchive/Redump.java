package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.github.openretrogamingarchive.Main.BASIC_DIR;
import static com.github.openretrogamingarchive.Main.NORMALIZED_DIR;
import static com.github.openretrogamingarchive.Main.ROOT_LATEST_DIR_PATH;
import static com.github.openretrogamingarchive.Util.downloadBytes;
import static com.github.openretrogamingarchive.Util.downloadToFile;
import static com.github.openretrogamingarchive.Util.getName;
import static com.github.openretrogamingarchive.Util.scrap;
import static com.github.openretrogamingarchive.Util.scrapOne;

public class Redump {

    private static final String ROOT_DIR = "Redump";

    private enum DownloadType { MainDat, BiosDat, Subchannels };

    public static void update() throws IOException {
        List<RedumpSystem> systems = getRedumpSystems();
        saveSystemDats(systems);
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

    private static void saveSystemDats(List<RedumpSystem> redumpSystems) throws IOException {
        // Normalized index
        Path normalizedRoot = ROOT_LATEST_DIR_PATH.resolve(Path.of(NORMALIZED_DIR, ROOT_DIR));
        if (!Files.exists(normalizedRoot)) {
            Files.createDirectories(normalizedRoot);
        }

        // Basic Index
        Path basicRoot = ROOT_LATEST_DIR_PATH.resolve(Path.of(BASIC_DIR, ROOT_DIR));
        if (!Files.exists(basicRoot)) {
            Files.createDirectories(basicRoot);
        }

        for (RedumpSystem redumpSystem: redumpSystems) {
            if (redumpSystem.getDatDownloadURL() != null) {
                processDat(DownloadType.MainDat, normalizedRoot, basicRoot, redumpSystem.getName(), redumpSystem.getDatDownloadURL());
            }

            if (redumpSystem.getSubChannelsSBIDatDownloadURL() != null) {
                processDat(DownloadType.Subchannels, normalizedRoot, basicRoot, redumpSystem.getName() + " - SBI Subchannels" , redumpSystem.getSubChannelsSBIDatDownloadURL());
            }

            if (redumpSystem.getBiosDatDownloadURL() != null) {
                processDat(DownloadType.BiosDat, normalizedRoot, basicRoot, redumpSystem.getName() + " - BIOS Images" , redumpSystem.getBiosDatDownloadURL());
            }
        }
    }

    private static void processDat(
            DownloadType downloadType,
            Path normalizedRoot,
            Path basicRoot,
            String normalizedSystemDirName,
            String downloadURL) throws IOException {
        // # Normalized
        Path normalizedSystemDir = normalizedRoot.resolve(normalizedSystemDirName);
        if (!Files.exists(normalizedSystemDir)) {
            Files.createDirectory(normalizedSystemDir);
        }
        Path datPath = downloadToFile(DOMAIN + downloadURL, normalizedSystemDir, true);

        // # Basic
        if (downloadType == DownloadType.Subchannels) {
            Path normalizedFromBasicLink = Path.of("../../" + NORMALIZED_DIR + "/" + ROOT_DIR + "/" + normalizedSystemDirName);
            Files.createSymbolicLink(basicRoot.resolve(Path.of(normalizedSystemDirName)), normalizedFromBasicLink);
        } else {
            Path normalizedFromBasicLink = Path.of("../../" + NORMALIZED_DIR + "/" + ROOT_DIR + "/" + normalizedSystemDirName + "/" + getName(datPath));
            Files.createSymbolicLink(basicRoot.resolve(Path.of(normalizedSystemDirName + ".dat")), normalizedFromBasicLink);
        }

    }


}
