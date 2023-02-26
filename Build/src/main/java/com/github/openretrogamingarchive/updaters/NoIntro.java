package com.github.openretrogamingarchive.updaters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.github.openretrogamingarchive.UpdaterBase;
import com.github.openretrogamingarchive.helpers.HTTP;
import com.github.openretrogamingarchive.helpers.Modified;
import com.github.openretrogamingarchive.helpers.ZIP;

public class NoIntro extends UpdaterBase {

    private static final Path DIR = NORMALIZED_DIR.resolve("No-Intro");
    private static final String URL = "https://datomatic.no-intro.org/";
    private static final String PAGE = "index.php?page=download&op=daily&s=64";
    private static final String FORM = "dat_type=standard&set1=Ok&valentine_day=Request";

    public NoIntro() throws URISyntaxException, IOException, InterruptedException {

	if (Modified.isOneDayOld(DIR)) {

	    System.out.print("Update No-Intro...");

	    // initiate http client and tell him to handle cookies and follow redirects (IMPORTANT for no-intro wesite)
	    final var http = new HTTP();

	    // ask download page for daily build overall archive using various form settings for this type of archive
	    final var resp1 = http.postFormExpectString(new URI(URL + PAGE), FORM);
	    System.out.print(".");

	    if (resp1.previousResponse().map(r -> r.statusCode() == 302).orElse(false)) {
		// download archive, cookie will be passed automatically from previous request so that server know what we asked before
		final var resp2 = http.postFormExpectInputStream(resp1.uri(), "lazy_mode=Download");
		System.out.print(".");
		
		// if application/zip content-type then extract archive
		if (HTTP.hasZip(resp2.headers())) {
		    try (final var zip = new ZIP(resp2.body())) {
			zip.extractAll(DIR);
			System.out.print(".");
			Modified.setModified(DIR);
			System.out.println("DONE");
		    }
		} else
		    System.out.println("FAILED (not zip)");
	    } else
		System.out.println("FAILED (status code unexpected)");
	} else
	    System.out.println("No-Intro not updated (already done less than 24 hours ago)... SKIP\n");
    }
}
