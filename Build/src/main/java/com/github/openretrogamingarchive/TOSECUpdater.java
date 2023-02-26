package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.github.openretrogamingarchive.Main.BASIC_DIR;
import static com.github.openretrogamingarchive.Main.NORMALIZED_DIR;
import static com.github.openretrogamingarchive.Main.ROOT_LATEST_DIR;
import static com.github.openretrogamingarchive.Util.downloadBytes;
import static com.github.openretrogamingarchive.Util.scrap;

public class TOSECUpdater {

    public static final String DOMAIN = "https://www.tosecdev.org";
    private static final String DOWNLOADS_URL = DOMAIN + "/downloads";
    public static void updateTOSEC() throws IOException {
        byte[] lastReleaseZip = getLastReleaseZip();
        Map<String, byte[]> files = Util.unZipInMemory(lastReleaseZip);

        // TOSEC
        unpackTOSECModule(files, "TOSEC");
        // TOSEC-ISO
        unpackTOSECModule(files, "TOSEC-ISO");
        // TOSEC-PIX
        unpackTOSECModule(files, "TOSEC-PIX");
    }

    private static void unpackTOSECModule(Map<String, byte[]> files, String moduleName) throws IOException {
        String script_move = new String(files.get("Scripts/move/" + moduleName + "_move.bat"), StandardCharsets.UTF_8);
        for (String move:script_move.split("\r\n")) {
            // # Normalized
            List<String> originToDestination = scrap(move, "\"", "\"");
            String fileName = originToDestination.get(0);
            String normalizedOrigin = fileName.substring(0, fileName.indexOf(" (TOSEC-v"));
            String normalizedDestination = ROOT_LATEST_DIR + "/" + NORMALIZED_DIR + "/" + moduleName + "/" + originToDestination.get(1).replace('\\', '/') + "/" + normalizedOrigin;
            Path normalizedDestinationPath = Path.of(normalizedDestination);
            if (!Files.exists(normalizedDestinationPath)) {
                Files.createDirectories(normalizedDestinationPath);
            }
            byte[] datBytes = files.get(moduleName + "/" + fileName);
            Path normalizedDatPath = normalizedDestinationPath.resolve(Path.of(fileName));
            Files.write(normalizedDatPath, datBytes);

            // # Basic
            String basicDestination = ROOT_LATEST_DIR + "/" + BASIC_DIR + "/" + moduleName + "/" + originToDestination.get(1).replace('\\', '/');
            Path basicDestinationPath = Path.of(basicDestination);
            if (!Files.exists(basicDestinationPath)) {
                Files.createDirectories(basicDestinationPath);
            }
            Path basicDestinationDatPath = basicDestinationPath.resolve(Path.of(normalizedOrigin + ".dat"));

            String relativeNormalizedDestination =  "../../../../../" + NORMALIZED_DIR + "/" + moduleName + "/" + originToDestination.get(1).replace('\\', '/') + "/" + normalizedOrigin;
            Path relativeNormalizedDestinationPath = Path.of(relativeNormalizedDestination);
            Path relativeDatPath = relativeNormalizedDestinationPath.resolve(Path.of(fileName));
            Files.createSymbolicLink(basicDestinationDatPath, relativeDatPath);
        }
    }

    private static byte[] getLastReleaseZip() throws IOException {
        String publicDownloadsPage = new String(downloadBytes(DOWNLOADS_URL), StandardCharsets.UTF_8);
        String releaseTable = scrap(publicDownloadsPage, "<div class=\"pd-categoriesbox\">", "<div class=\"pd-categoriesbox\">").get(0);
        List<String> releases = scrap(releaseTable, "<a href=\"", "\">");
        String lastReleaseURL = DOMAIN + releases.get(1);
        String lastReleaseDownloadsPage = new String(downloadBytes(lastReleaseURL), StandardCharsets.UTF_8);
        String downloadURL = DOMAIN + "/downloads/category/" + scrap(lastReleaseDownloadsPage, "href=\"/downloads/category/", "\"").get(1);
        return Util.downloadBytes(downloadURL);
    }
}
