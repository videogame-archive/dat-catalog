package com.github.openretrogamingarchive;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.openretrogamingarchive.helpers.CSV;

public class Indexes extends UpdaterBase {
	
	static void update() throws IOException {
		update(ROOT_DIR);
	}
	
	static void update(Path current) throws IOException {
		List<String[]> currentIndex = new ArrayList<>();
		try (final var stream = Files.list(current)) {
		    stream.filter(s -> !s.getFileName().toString().startsWith(".")).forEach(fileInDir -> {
			try {
			    URL url = null;
			    if (Files.isDirectory(fileInDir)) {
				update(fileInDir);
			    } else {
				url = new URL(BASE_URL + BASE_DIR.relativize(fileInDir));
			    }
			    currentIndex.add(CSV.toRow(fileInDir, url));
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    });
		}
		CSV.save(current, currentIndex);
	}

}
