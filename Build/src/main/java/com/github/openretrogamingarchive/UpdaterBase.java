package com.github.openretrogamingarchive;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class UpdaterBase {
    
    protected static final Path NORMALIZED_DIR = Paths.get("../root/normalized").toAbsolutePath();

    protected UpdaterBase() {
    }
}
