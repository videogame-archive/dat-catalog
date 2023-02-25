package com.github.openretrogamingarchive;

import java.io.IOException;

public class Main {

    private static final String ROOT_LATEST_DIR = "../root/normalized";

    public static void main(String[] args) throws IOException {
        RedumpUpdater.updateRedump(ROOT_LATEST_DIR);
        TOSECUpdater.updateTOSEC(ROOT_LATEST_DIR);
    }
}