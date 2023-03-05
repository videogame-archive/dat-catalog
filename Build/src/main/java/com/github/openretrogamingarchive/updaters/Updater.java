package com.github.openretrogamingarchive.updaters;

import java.nio.file.Path;

public abstract class Updater {

    public static final Path BASE_DIR = Path.of("..");

    public static final Path ROOT_DIR = BASE_DIR.resolve("root");

    public static final Path NORMALIZED_DIR = ROOT_DIR.resolve("normalized");

    public static final Path BASIC_DIR = ROOT_DIR.resolve("basic");

    public static final String BASE_URL = "https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/";

    public abstract Path[] getTrackedFolders();

    public abstract void update() throws Exception;

    protected Updater() {
    }
}
