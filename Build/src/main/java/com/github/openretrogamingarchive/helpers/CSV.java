package com.github.openretrogamingarchive.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class CSV {
    private CSV() {
    }

    private enum Headers {
        Type, Name, URL, CRC, Size // NOSONAR
    }

    private enum Type {
        FILE, DIRECTORY
    }

    public static String[] toRow(Path path, URL url) throws IOException {
        Type type = null;
        Long size = null;
        Long crc = null;

        if (Files.isDirectory(path)) {
            type = Type.DIRECTORY;
        } else {
            type = Type.FILE;
            Path realPath = path.toRealPath(); // It resolves symlinks
            size = Files.size(realPath);
            CRC32 crc32 = new CRC32();
            crc32.update(Files.readAllBytes(realPath));
            crc = crc32.getValue();
        }

        String urlAsString = "";
        if (url != null) {
            urlAsString = url.toString();
        }
        String crcAsString = "";
        if (crc != null) {
            crcAsString = Long.toString(crc);
        }
        String sizeAsString = "";
        if (size != null) {
            sizeAsString = Long.toString(size);
        }

        return new String[] { type.name(), getLastPathName(path), urlAsString, crcAsString, sizeAsString };
    }

    public static void save(Path root, List<String[]> csvRows) throws IOException {
        final var csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(Headers.class).build();
        Path index = root.resolve(".index.csv");
        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(index.toFile(), StandardCharsets.UTF_8), csvFormat)) {
            csvPrinter.printRecords(csvRows);
        }
    }

    public static String getLastPathName(Path path) {
        return path.getName(path.getNameCount() - 1).toString();
    }

}
