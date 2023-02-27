package com.github.openretrogamingarchive;

import java.nio.file.Path;

public abstract class UpdaterBase {
    
	protected static final Path BASE_DIR = Path.of("..");
	
	protected static final Path ROOT_DIR = BASE_DIR.resolve("root");

	protected static final Path NORMALIZED_DIR = ROOT_DIR.resolve("normalized");

	protected static final Path BASIC_DIR = ROOT_DIR.resolve("basic");

	public static final String BASE_URL = "https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/";

    protected UpdaterBase() {
    }
}
