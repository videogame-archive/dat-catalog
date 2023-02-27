package com.github.openretrogamingarchive.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

public class Modified {

	private Modified() {

	}

	public static Date getNow() {
		return new Date();
	}

	public static Date getModified(Path path) {
		final var file = path.resolve(".modified");
		if (Files.exists(file)) {
			try {
				return Date.from(Instant.parse(Files.readString(file)));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		return new Date(0);
	}

	public static void setModified(Path path) throws IOException {
		Files.writeString(path.resolve(".modified"), Instant.now().toString());
	}

	public static boolean isOneDayOld(Path path) {
		return isOneDayOld(getModified(path));
	}

	public static boolean isOneDayOld(Date date) {
		return getNow().getTime() - date.getTime() > 86_400_000;
	}
}
