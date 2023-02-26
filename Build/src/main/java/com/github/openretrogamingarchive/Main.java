package com.github.openretrogamingarchive;

import java.io.IOException;

public class Main {

    public static final String ROOT_LATEST_DIR = "../root/";

    public static final String NORMALIZED_DIR = "normalized";

    public static final String BASIC_DIR = "basic";

    public static void main(String[] args) throws IOException {
        RedumpUpdater.updateRedump(ROOT_LATEST_DIR);
        TOSECUpdater.updateTOSEC(ROOT_LATEST_DIR);
    }
}