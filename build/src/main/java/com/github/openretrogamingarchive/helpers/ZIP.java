package com.github.openretrogamingarchive.helpers;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZIP implements Closeable {
    private ZipInputStream zis;
    private Optional<String> name;

    public ZIP(InputStream in, Optional<String> name) {
        this.zis = new ZipInputStream(in);
        this.name = name;
    }

    public ZipEntry extractFirstFile(Path parent) throws IOException {
        final var ze = zis.getNextEntry();
        if (ze != null) {
            final var file = parent.resolve(ze.getName());
            try (final var out = Files.newOutputStream(file)) {
                zis.transferTo(out);
            }
        }
        return ze;
    }

    public Path extractAll(Path parent) throws IOException {
        ZipEntry ze;
        Path path = null;
        while ((ze = zis.getNextEntry()) != null) {
            path = parent.resolve(ze.getName());
            if (ze.isDirectory()) {
                Files.createDirectories(path);
            } else {
                Files.createDirectories(path.getParent());
                try (final var out = Files.newOutputStream(path)) {
                    zis.transferTo(out);
                }
            }
        }
        return path;
    }

    public Path extractSmart(Path parent) throws IOException {
        final var lastParentFolder = parent.getFileName();
        final Optional<String> zipbasename = name.map(ZIP::basename);
        final var skipSmart = zipbasename.map(n -> n.equals(lastParentFolder.toString())).orElse(true);    // skip smart if zip basename is equal to parent last folder name
        boolean firstEntry = true;
        boolean insertBasename = false;
        ZipEntry ze;
        Path path = null;
        while ((ze = zis.getNextEntry()) != null) {
            final var entryPath = Path.of(ze.getName());
            if (Boolean.FALSE.equals(skipSmart) && !ze.isDirectory()) {
                // zip basename become intermediate folder if first entry basename is not of the same as zip basename
                if (firstEntry) {
                    if (zipbasename.isPresent() && !zipbasename.get().equals(basename(entryPath)))
                    	insertBasename = true;
                    firstEntry = false;
                }
            }
            if(insertBasename)
            	path = parent.resolve(zipbasename.get()).resolve(entryPath);
            else
                path = parent.resolve(entryPath);
            if (ze.isDirectory()) {
                Files.createDirectories(path);
            } else {
                Files.createDirectories(path.getParent());
                try (final var out = Files.newOutputStream(path)) {
                    zis.transferTo(out);
                }
            }
        }
        return path;
    }

    public static Map<String, byte[]> extractInMemory(InputStream is) throws IOException {
        Map<String, byte[]> inMemory = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(is, StandardCharsets.ISO_8859_1)) { // UTF_8 breaks TOSEC
            ZipEntry zipEntry = null;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    inMemory.put(zipEntry.getName(), zis.readNBytes(((Long) zipEntry.getSize()).intValue()));
                }
            }
        }
        return inMemory;
    }

    private static Pattern extPattern = Pattern.compile("(?<!^)[.][^.]*$");
    private static Pattern extPatternAll = Pattern.compile("(?<!^)[.][a-zA-Z0-9.]*$");

    public static String basename(Path path) {
        return basename(path.getFileName().toString(), true);
    }

    public static String basename(String filename) {
        return basename(filename, true);
    }

    public static String basename(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty())
            return filename;
        if (removeAllExtensions)
            return extPatternAll.matcher(filename).replaceAll("");
        return extPattern.matcher(filename).replaceAll("");
    }

    @Override
    public void close() throws IOException {
        zis.close();
    }
}
