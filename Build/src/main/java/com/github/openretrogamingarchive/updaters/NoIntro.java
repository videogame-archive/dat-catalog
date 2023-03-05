package com.github.openretrogamingarchive.updaters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.openretrogamingarchive.helpers.HTTP;
import com.github.openretrogamingarchive.helpers.ZIP;

public class NoIntro extends UpdaterBase {
	private static final String ROOT_DIR = "No-Intro";

	private static final String URL = "https://datomatic.no-intro.org/";
	private static final String PAGE = "index.php?page=download&op=daily&s=64";
	private static final String FORM = "dat_type=standard&set1=Ok&valentine_day=Request";

	public static void update() throws URISyntaxException, IOException, InterruptedException {

		System.out.print("Update No-Intro...");

		// initiate http client and tell him to handle cookies and follow redirects
		// (IMPORTANT for no-intro wesite)
		final var http = new HTTP();

		// ask download page for daily build overall archive using various form settings
		// for this type of archive
		final var resp1 = http.postFormExpectString(new URI(URL + PAGE), FORM);
		System.out.print(".");

		if (resp1.previousResponse().map(r -> r.statusCode() == 302).orElse(false)) {
			// download archive, cookie will be passed automatically from previous request
			// so that server know what we asked before
			final var resp2 = http.postFormExpectInputStream(resp1.uri(), "lazy_mode=Download");
			System.out.print(".");

			// if application/zip content-type then extract archive
			if (HTTP.hasZip(resp2.headers())) {
				Map<String, byte[]> resp2Files = ZIP.extractInMemory(resp2.body());
				for (String pathToDat:resp2Files.keySet()) {
					if (pathToDat.endsWith(".dat")) {
						int systemStart = pathToDat.lastIndexOf('/') + 1;
						int systemEnd = pathToDat.lastIndexOf(' ');
						String noIntroSystemCategory = pathToDat.substring(0, systemStart - 1);
						String datFileName = pathToDat.substring(systemStart);

						String noIntroSystem = null;

						if (noIntroSystemCategory.equals("Redump Custom")) {
							noIntroSystem = pathToDat.substring(systemStart, pathToDat.indexOf("(") - 1);
						} else {
							noIntroSystem = pathToDat.substring(systemStart, systemEnd);
						}

						// # Normalized
						Path normalized = NORMALIZED_DIR.resolve(ROOT_DIR).resolve(noIntroSystemCategory).resolve(noIntroSystem);
						if (!Files.exists(normalized)) {
							Files.createDirectories(normalized);
						}
						Files.write(normalized.resolve(datFileName), resp2Files.get(pathToDat));

						// # Basic
						Path basic = BASIC_DIR.resolve(ROOT_DIR).resolve(noIntroSystemCategory);
						if (!Files.exists(basic)) {
							Files.createDirectories(basic);
						}
						Files.write(basic.resolve(noIntroSystem + ".dat"), resp2Files.get(pathToDat));
					}
				}
			} else
				System.out.println("FAILED (not zip)");
		} else
			System.out.println("FAILED (status code unexpected)");
	}
}
