package com.github.openretrogamingarchive.helpers;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZIP implements Closeable {
	private ZipInputStream zis;

	public ZIP(InputStream in) {
		zis = new ZipInputStream(in);
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

	@Override
	public void close() throws IOException {
		zis.close();
	}
}
