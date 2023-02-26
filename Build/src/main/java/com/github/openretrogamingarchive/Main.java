package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static final String ROOT_LATEST_DIR = "../root/";

    public static final Path ROOT_LATEST_DIR_PATH = Path.of("../root/");

    public static final String NORMALIZED_DIR = "normalized";

    public static final String BASIC_DIR = "basic";

    public static void main(String[] args) throws IOException {
        RedumpUpdater.updateRedump();
        TOSECUpdater.updateTOSEC();
    }
}