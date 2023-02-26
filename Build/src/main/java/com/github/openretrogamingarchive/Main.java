package com.github.openretrogamingarchive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.openretrogamingarchive.Util.row;

public class Main {

    public static final String ROOT_LATEST_DIR = "../root/";

    public static final Path ROOT_LATEST_DIR_PATH = Path.of("../root/");

    public static final String NORMALIZED_DIR = "normalized";

    public static final String BASIC_DIR = "basic";

    public static void main(String[] args) throws IOException {
        Redump.update();
        TOSEC.update();
        updateIndexes();
    }

    private static void updateIndexes() throws IOException {
        Deque<Path> dirsToMakeIndexesFor = new LinkedList<>();
        dirsToMakeIndexesFor.add(ROOT_LATEST_DIR_PATH);

        while (!dirsToMakeIndexesFor.isEmpty()) {
            Path current = dirsToMakeIndexesFor.removeFirst();
            List<String[]> currentIndex = new ArrayList<>();
            List<Path> filesInDir = Files.list(current).toList();
            for (Path fileInDir:filesInDir) {
                if (Files.isDirectory(fileInDir)) {
                    dirsToMakeIndexesFor.add(fileInDir);
                }
                currentIndex.add(row(fileInDir));
            }
            Util.saveCSV(current, currentIndex);
        }

    }

}