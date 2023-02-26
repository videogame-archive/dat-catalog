package com.github.openretrogamingarchive.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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

    public record Row(Type type, String name, URL url, long crc, long size) {
	public static Row dir(String name) {
	    return new Row(Type.DIRECTORY, name, null, 0, 0);
	}
    }

    public static void save(Path root, Iterable<Row> csvRows) throws IOException {
	final var csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader(Headers.class).build();
	Path index = root.resolve("index.csv");
	try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(index.toFile(), StandardCharsets.UTF_8), csvFormat)) {
	    csvPrinter.printRecords(csvRows);
	}
    }
}
