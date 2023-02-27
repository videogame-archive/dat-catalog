package com.github.openretrogamingarchive.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;
import java.util.regex.Pattern;

public class HTTP {
	private HttpClient client;

	public HTTP() {
		final var builder = HttpClient.newBuilder();
		builder.followRedirects(HttpClient.Redirect.NORMAL);
		builder.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		client = builder.build();
	}

	public HttpResponse<String> postFormExpectString(URI uri, String formdata)
			throws IOException, InterruptedException {
		final var req = HttpRequest.newBuilder().uri(uri).headers("Content-Type", "application/x-www-form-urlencoded")
				.POST(BodyPublishers.ofString(formdata)).build();
		return client.send(req, HttpResponse.BodyHandlers.ofString());
	}

	public HttpResponse<InputStream> postFormExpectInputStream(URI uri, String formdata)
			throws IOException, InterruptedException {
		final var req = HttpRequest.newBuilder().uri(uri).headers("Content-Type", "application/x-www-form-urlencoded")
				.POST(BodyPublishers.ofString(formdata)).build();
		return client.send(req, HttpResponse.BodyHandlers.ofInputStream());
	}

	public static final boolean hasZip(HttpHeaders headers) {
		return headers.firstValue("content-type").map(v -> v.equals("application/zip")).orElse(false);
	}

	private static final Pattern contentDisposition = Pattern.compile("filename=\"(.*?)\"");

	public static final Optional<String> extractFilename(HttpHeaders header) {
		return header.firstValue("content-disposition").map(value -> {
			final var matcher = contentDisposition.matcher(value);
			if (matcher.matches() && matcher.groupCount() == 1) {
				return matcher.group(1);
			}
			return null;
		});
	}

	public static final void writeToFile(HttpResponse<InputStream> resp, Path file) throws IOException {
		try (final var out = Files.newOutputStream(file)) {
			resp.body().transferTo(out);
		}
	}
}
