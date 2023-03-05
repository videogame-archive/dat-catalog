package com.github.openretrogamingarchive;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.openretrogamingarchive.helpers.CSV;

import static com.github.openretrogamingarchive.updaters.Updater.BASE_DIR;
import static com.github.openretrogamingarchive.updaters.Updater.BASE_URL;

public class Indexes {

	public static List<String[]> update(Path current, boolean print) throws IOException {
		List<String[]> currentIndex = new ArrayList<>();
		try (final var stream = Files.list(current)) {
		    stream.filter(s -> !s.getFileName().toString().startsWith(".")).forEach(fileInDir -> {
			try {
				if (!Files.isDirectory(fileInDir)) {
					URL url = new URL(BASE_URL + BASE_DIR.relativize(fileInDir));
					currentIndex.add(CSV.toRow(fileInDir, url));
				} else {
					currentIndex.add(CSV.toRow(fileInDir, null));
					List<String[]> indexes = update(fileInDir, false);
					currentIndex.addAll(indexes);
				}
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    });
		}
		if (print) {
			CSV.save(current, currentIndex);
		}
		return currentIndex;
	}

}
