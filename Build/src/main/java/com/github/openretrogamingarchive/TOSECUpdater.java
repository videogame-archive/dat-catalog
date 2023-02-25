package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.openretrogamingarchive.Util.downloadBytes;
import static com.github.openretrogamingarchive.Util.scrap;

public class TOSECUpdater {

    public static final String DOMAIN = "https://www.tosecdev.org";

    private static final String DOWNLOADS_URL = DOMAIN + "/downloads";
    public static void updateTOSEC(String pathToRoot) throws IOException {
        byte[] lastReleaseZip = getLastRelease();
        int breakpoint = 0;
    }

    private static byte[] getLastRelease() throws IOException {
        String publicDownloadsPage = new String(downloadBytes(DOWNLOADS_URL), StandardCharsets.UTF_8);
        String releaseTable = scrap(publicDownloadsPage, "<div class=\"pd-categoriesbox\">", "<div class=\"pd-categoriesbox\">").get(0);
        List<String> releases = scrap(releaseTable, "<a href=\"", "\">");
        String lastReleaseURL = DOMAIN + releases.get(1);
        return Util.downloadBytes(lastReleaseURL);
    }
}
