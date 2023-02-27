package com.github.openretrogamingarchive.helpers;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

	public void extractAll(Path parent) throws IOException {
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			final var path = parent.resolve(ze.getName());
			if (ze.isDirectory()) {
				Files.createDirectories(path);
			} else {
				Files.createDirectories(path.getParent());
				try (final var out = Files.newOutputStream(path)) {
					zis.transferTo(out);
				}
			}
		}
	}
	
	public void extractSmart(Path parent) throws IOException {
		final var lastParentFolder = parent.getFileName();
		final Optional<String> zipbasename = name.map(ZIP::basename);
		final var skipSmart = zipbasename.map(n -> n.equals(lastParentFolder.toString())).orElse(true);	// skip smart if zip basename is equal to parent last folder name	
		boolean firstEntry = true;
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			final var entryPath = Path.of(ze.getName());
			Path path = parent.resolve(entryPath.getParent());
			if (!skipSmart) {
				// zip basename become intermediate folder if first entry basename is not of the same as zip basename
				if (firstEntry) {
					if (!basename(entryPath).equals(zipbasename.get()))
						path = parent.resolve(zipbasename.get()).resolve(entryPath.getParent());
					firstEntry = false;
				}
			}
			if (ze.isDirectory()) {
				Files.createDirectories(path);
			} else {
				Files.createDirectories(path.getParent());
				try (final var out = Files.newOutputStream(path)) {
					zis.transferTo(out);
				}
			}
		}
	}
	
	private static Pattern extPattern = Pattern.compile("(?<!^)[.][^.]*$"); 
	private static Pattern extPatternAll = Pattern.compile("(?<!^)[.].*"); 
	
	public static String basename(Path path) {
		return basename(path.getFileName().toString(), false);
	}
	public static String basename(String filename) {
		return basename(filename, false);
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
