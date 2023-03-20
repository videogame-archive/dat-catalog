package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Predicate;

import com.github.openretrogamingarchive.helpers.Util;
import com.github.openretrogamingarchive.updaters.Updater;

public class Pages {

	private static final Path DOCS_DIR = Updater.BASE_DIR.resolve("docs");

	private static final Comparator<? super Path> pathSorter = (a, b) -> {
		if(!Files.isDirectory(a) && Files.isDirectory(b))
			return 1;
		if(Files.isDirectory(a) && !Files.isDirectory(b))
			return -1;
		return a.toString().compareToIgnoreCase(b.toString());
	};

	private static final Predicate<? super Path> pathFilter = s -> {
		if(Files.isDirectory(s))
			return true;
		final var filename = s.getFileName().toString();
		final var index = filename.lastIndexOf('.');
		if (index <= 0)
			return false;
		final var ext = filename.substring(index).toLowerCase();
		return ext.equals(".dat") || ext.equals(".xml");
	};

	private Pages() {

	}

	public static void update(Path current) throws IOException {
		if (Updater.ROOT_DIR.equals(current)) {
			Util.deleteRecursive(DOCS_DIR);
			Files.createDirectories(DOCS_DIR);
		}
		try (final var index = Files.newBufferedWriter(DOCS_DIR.resolve(Updater.ROOT_DIR.relativize(current)).resolve("index.md"))) {
			index.write("<table>\n");
			index.write("<tr><th>Name</th><th>Size</th></tr>\n");
			try (final var stream = Files.list(current)) {
					stream.filter(pathFilter).sorted(pathSorter).forEachOrdered(fileInDir -> {
					try {
						if (!Files.isDirectory(fileInDir)) {
							final var file = Updater.BASE_URL + Updater.BASE_DIR.relativize(fileInDir);
							index.write("<tr><td>%n[%s](%s)%n</td><td>%d</td></tr>%n".formatted(fileInDir.getFileName().toString(),	file, Files.size(fileInDir)));
						} else {
							final var relativized = Updater.ROOT_DIR.relativize(fileInDir);
							index.write("<tr><td>%n[%s](%s)%n</td><td>%s</td></tr>%n".formatted(fileInDir.getFileName().toString(),	fileInDir.getFileName().resolve("index.html").toString(), "DIR"));
							Files.createDirectories(DOCS_DIR.resolve(relativized));
							update(fileInDir);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			index.write("</table>\n");
		}
	}
}
