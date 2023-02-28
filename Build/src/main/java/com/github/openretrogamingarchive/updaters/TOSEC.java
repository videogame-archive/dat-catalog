package com.github.openretrogamingarchive.updaters;

import static com.github.openretrogamingarchive.Util.scrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.github.openretrogamingarchive.UpdaterBase;
import com.github.openretrogamingarchive.helpers.HTTP;
import com.github.openretrogamingarchive.helpers.ZIP;

public class TOSEC extends UpdaterBase {

    public static final String DOMAIN = "https://www.tosecdev.org";
    private static final String DOWNLOADS_URL = DOMAIN + "/downloads";
    public static void update() throws IOException, InterruptedException, URISyntaxException {
        Map<String, byte[]> files = ZIP.extractInMemory(getLastReleaseZip());

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
            String path = originToDestination.get(1).replace('\\', '/');
            String normalizedOrigin = fileName.substring(0, fileName.indexOf(" (TOSEC-v"));
            Path normalizedDestinationPath = NORMALIZED_DIR.resolve(moduleName).resolve(path).resolve(normalizedOrigin);
            if (!Files.exists(normalizedDestinationPath)) {
                Files.createDirectories(normalizedDestinationPath);
            }
            byte[] datBytes = files.get(moduleName + "/" + fileName);
            Path normalizedDatPath = normalizedDestinationPath.resolve(Path.of(fileName));
            Files.write(normalizedDatPath, datBytes);

            // # Basic
            Path basicDestinationPath = BASIC_DIR.resolve(moduleName).resolve(path);
            if (!Files.exists(basicDestinationPath)) {
                Files.createDirectories(basicDestinationPath);
            }
            Path basicDestinationDatPath = basicDestinationPath.resolve(Path.of(normalizedOrigin + ".dat"));

            int relativeBacktrackLength = path.split("/").length + 2;
            String relativeBackgrack = "";
            for (int i = 0; i < relativeBacktrackLength; i++) {
                relativeBackgrack += "../";
            }
            String relativeNormalizedDestination =  relativeBackgrack + NORMALIZED_DIR + "/" + moduleName + "/" + path + "/" + normalizedOrigin;
            Path relativeNormalizedDestinationPath = Path.of(relativeNormalizedDestination);
            Path relativeDatPath = relativeNormalizedDestinationPath.resolve(Path.of(fileName));
            Files.createSymbolicLink(basicDestinationDatPath, relativeDatPath);
        }
    }

    private static InputStream getLastReleaseZip() throws IOException, InterruptedException, URISyntaxException {
        String publicDownloadsPage = HTTP.downloadAsString(DOWNLOADS_URL);
        String releaseTable = scrap(publicDownloadsPage, "<div class=\"pd-categoriesbox\">", "<div class=\"pd-categoriesbox\">").get(0);
        List<String> releases = scrap(releaseTable, "<a href=\"", "\">");
        String lastReleaseURL = DOMAIN + releases.get(1);
        String lastReleaseDownloadsPage = HTTP.downloadAsString(lastReleaseURL);
        String downloadURL = DOMAIN + "/downloads/category/" + scrap(lastReleaseDownloadsPage, "href=\"/downloads/category/", "\"").get(1);
        return HTTP.downloadAsStream(downloadURL);
    }
}
