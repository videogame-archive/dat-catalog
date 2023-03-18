package com.github.openretrogamingarchive.updaters;

import static com.github.openretrogamingarchive.helpers.Util.scrap;
import static com.github.openretrogamingarchive.helpers.Util.scrapOne;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.openretrogamingarchive.helpers.HTTP;
import com.github.openretrogamingarchive.helpers.HTTP.ZIPMode;
import com.github.openretrogamingarchive.helpers.Util;

public class Redump extends Updater {

    private static final String REDUMP_DIR = "Redump";
    private enum DownloadType {MainDat, BiosDat, Subchannels};

    public void update() throws Exception {
        List<RedumpSystem> systems = getRedumpSystems();
        saveSystemDats(systems);
    }

    public Path[] getTrackedFolders() {
        return new Path[]{NORMALIZED_DIR.resolve(REDUMP_DIR), BASIC_DIR.resolve(REDUMP_DIR)};
    }

    private static class RedumpSystem {
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

    private static List<RedumpSystem> getRedumpSystems() throws IOException, InterruptedException, URISyntaxException {
        String publicRedumpDownloadsPage = HTTP.downloadAsString(DOWNLOADS_URL);
        String systemsTable = scrap(publicRedumpDownloadsPage, "<table class=\"statistics\" cellspacing=\"0\">", "</table>").get(0);
        List<String> systems = scrap(systemsTable, Util.PATTERN_TR);
        systems.remove(0);

        List<RedumpSystem> redumpSystems = new ArrayList<>(systems.size());
        for (String system : systems) {
            List<String> systemInfo = scrap(system, Util.PATTERN_TD);
            String name = systemInfo.get(0);
            String dat = scrapOne(systemInfo.get(2), Util.PATTERN_A_HREF);
            String subDat = scrapOne(systemInfo.get(3), Util.PATTERN_A_HREF);
            String biosDat = scrapOne(systemInfo.get(5), Util.PATTERN_A_HREF);
            redumpSystems.add(new RedumpSystem(name, dat, subDat, biosDat));
        }
        return redumpSystems;
    }

    private static void saveSystemDats(List<RedumpSystem> redumpSystems) throws IOException, InterruptedException, URISyntaxException {
        // Normalized
        Path normalizedRoot = NORMALIZED_DIR.resolve(REDUMP_DIR);
        if (!Files.exists(normalizedRoot)) {
            Files.createDirectories(normalizedRoot);
        }

        // Basic
        Path basicRoot = BASIC_DIR.resolve(REDUMP_DIR);
        if (!Files.exists(basicRoot)) {
            Files.createDirectories(basicRoot);
        }

        for (RedumpSystem redumpSystem : redumpSystems) {
            if (redumpSystem.getDatDownloadURL() != null) {
                processDat(DownloadType.MainDat, normalizedRoot, basicRoot, redumpSystem.getName(), redumpSystem.getDatDownloadURL());
            }

            if (redumpSystem.getSubChannelsSBIDatDownloadURL() != null) {
                processDat(DownloadType.Subchannels, normalizedRoot, basicRoot, redumpSystem.getName() + " - SBI Subchannels", redumpSystem.getSubChannelsSBIDatDownloadURL());
            }

            if (redumpSystem.getBiosDatDownloadURL() != null) {
                processDat(DownloadType.BiosDat, normalizedRoot, basicRoot, redumpSystem.getName() + " - BIOS Images", redumpSystem.getBiosDatDownloadURL());
            }
        }
    }

    private static void processDat(
            DownloadType downloadType,
            Path normalizedRoot,
            Path basicRoot,
            String normalizedSystemDirName,
            String downloadURL) throws IOException, InterruptedException, URISyntaxException {
        // # Normalized
        Path normalizedSystemDir = normalizedRoot.resolve(normalizedSystemDirName);
        if (!Files.exists(normalizedSystemDir)) {
            Files.createDirectory(normalizedSystemDir);
        }
        Path datPath = HTTP.downloadToFolder(new URI(DOMAIN + downloadURL), normalizedSystemDir, ZIPMode.ALL);

        // # Basic
        if (downloadType == DownloadType.Subchannels) {
            Util.createSymbolicLink(datPath, basicRoot.resolve(Path.of(normalizedSystemDirName)));
        } else {
            Util.createSymbolicLink(datPath, basicRoot.resolve(Path.of(normalizedSystemDirName + ".dat")));
        }

    }

}
