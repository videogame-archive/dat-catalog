package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.openretrogamingarchive.Util.downloadBytes;
import static com.github.openretrogamingarchive.Util.scrap;

public class TOSECUpdater {
    private static final String DOWNLOAD_URL = "https://www.tosecdev.org/downloads";
    public static void updateTOSEC(String pathToRoot) throws IOException {
        String publicDownloadsPage = new String(downloadBytes(DOWNLOAD_URL), StandardCharsets.UTF_8);
        String releaseTable = scrap(publicDownloadsPage, "<div class=\"pd-categoriesbox\">", "<div class=\"pd-categoriesbox\">").get(0);
        List<String> releases = scrap(releaseTable, "<a href=\"", "\">");
    }
}
