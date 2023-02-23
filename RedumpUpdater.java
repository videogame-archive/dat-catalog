import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RedumpUpdater {

    private static final String ROOT_LATEST_DIR = "latest";

    private static final String ROOT_REDUMP_DIR = "Redump";

    public static void main(String[] args) throws IOException {
        List<RedumpSystem> systems = getRedumpSystems();
        for (RedumpSystem system:systems) {
            saveSystemDat(Path.of(ROOT_LATEST_DIR, ROOT_REDUMP_DIR), system);
        }
        saveSystemsIndex(Path.of(ROOT_LATEST_DIR, ROOT_REDUMP_DIR), systems);
    }

    public static class RedumpSystem {
        private final String name;
        private final String datDownloadURL;
        private final String biosDatDownloadURL;

        public RedumpSystem(String name, String datDownloadURL, String biosDatDownloadURL) {
            this.name = name;
            this.datDownloadURL = datDownloadURL;
            this.biosDatDownloadURL = biosDatDownloadURL;
        }

        public String getName() {
            return name;
        }

        public String getDatDownloadURL() {
            return datDownloadURL;
        }

        public String getBiosDatDownloadURL() {
            return biosDatDownloadURL;
        }
    }

    public static final String REDUMP_DOMAIN = "http://redump.org";
    public static final String REDUMP_DOWNLOADS_URL = REDUMP_DOMAIN + "/downloads/";
    private static List<RedumpSystem> getRedumpSystems() throws IOException {

        String publicRedumpDownloadsPage = new String(downloadIndex(REDUMP_DOWNLOADS_URL), StandardCharsets.UTF_8);
        String systemsTable = scrap(publicRedumpDownloadsPage, "<table class=\"statistics\" cellspacing=\"0\">", "</table>").get(0);
        List<String> systems = scrap(systemsTable, "<tr>", "</tr>");
        systems.remove(0);

        List<RedumpSystem> redumpSystems = new ArrayList<>(systems.size());
        for (String system:systems) {
            List<String> systemInfo = scrap(system, "<td>", "</td>");
            String name = systemInfo.get(0);
            String dat = scrapOne(systemInfo.get(2), "<a href=\"", "\">");
            String biosDat = scrapOne(systemInfo.get(5), "<a href=\"", "\">");
            redumpSystems.add(new RedumpSystem(name, dat, biosDat));
        }
        return redumpSystems;
    }

    private static void saveSystemDat(Path redump, RedumpSystem redumpSystem) throws IOException {
        if (!Files.exists(redump)) {
            Files.createDirectories(redump);
        }

        if (redumpSystem.getDatDownloadURL() != null) {
            Path redumpSystemDir = redump.resolve(redumpSystem.getName());
            if (!Files.exists(redumpSystemDir)) {
                Files.createDirectory(redumpSystemDir);
            }
            Path redumpSystemDat = downloadDat(REDUMP_DOMAIN + redumpSystem.getDatDownloadURL(), redumpSystemDir);
            saveDatIndex(redumpSystemDir, redumpSystemDat);
        }

        if (redumpSystem.getBiosDatDownloadURL() != null) {
            Path redumpSystemDir = redump.resolve(redumpSystem.getName() + " - BIOS Images");
            if (!Files.exists(redumpSystemDir)) {
                Files.createDirectory(redumpSystemDir);
            }
            Path redumpBiosDat = downloadDat(REDUMP_DOMAIN + redumpSystem.getDatDownloadURL(), redumpSystemDir);
            saveDatIndex(redumpSystemDir, redumpBiosDat);
        }
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

    private static byte[] downloadIndex(String url) throws IOException {
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

    private static Path downloadDat(String url, Path parent) throws IOException {
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
        String contentDisposition = conn.getHeaderField("Content-Disposition");
        String fileName = scrapOne(contentDisposition, "\"","\"");
        Path file = parent.resolve(fileName);
        return Files.write(file, outStream.toByteArray());
    }

    //
    // CSV Generation Helper methods
    //

    private static final String CSV_HEADERS = "\"type\", \"name\", \"download url\"";
    private static final String LF = "\n";
    private enum Type { FILE, FOLDER };

    private static String escape(String string) {
        return "\"" + string + "\"";
    }
    private static void saveSystemsIndex(Path redump, List<RedumpSystem> redumpSystems) throws IOException {
        StringBuilder rootIndex = new StringBuilder();
        rootIndex.append(CSV_HEADERS).append(LF);
        for (RedumpSystem redumpSystem:redumpSystems) {
            if (redumpSystem.getDatDownloadURL() != null) {
                rootIndex.append(escape(Type.FOLDER.name())).append(",").append(escape(redumpSystem.getName())).append(",").append(LF);
            }

            if (redumpSystem.getBiosDatDownloadURL() != null) {
                rootIndex.append(escape(Type.FOLDER.name())).append(",").append(escape(redumpSystem.getName() + " - BIOS Images")).append(",").append(LF);
            }
        }
        Path redumpIndex = redump.resolve("index.csv");
        Files.write(redumpIndex, rootIndex.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static final String DOWNLOAD_URL_TEMPLATE = "https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/Redump/";
    private static void saveDatIndex(Path redumpSystemDir, Path redumpSystemDat) throws IOException {
        String redumpSystemDirName = redumpSystemDir.getName(redumpSystemDir.getNameCount() - 1).toString();
        String redumpSystemDatName = redumpSystemDat.getName(redumpSystemDat.getNameCount() - 1).toString();
        StringBuilder indexContent = new StringBuilder();
        indexContent.append(CSV_HEADERS).append(LF);
        indexContent.append(escape(Type.FILE.name())).append(",");
        indexContent.append(escape(redumpSystemDatName)).append(",");
        indexContent.append(escape(DOWNLOAD_URL_TEMPLATE + redumpSystemDirName + "/" + redumpSystemDatName));
        indexContent.append(LF);
        Path redumpIndex = redumpSystemDir.resolve("index.csv");
        Files.write(redumpIndex, indexContent.toString().getBytes(StandardCharsets.UTF_8));
    }
}
