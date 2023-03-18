package com.github.openretrogamingarchive.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Util {
    //
    // Text Processing Helper Methods
    //

    public static String scrapOne(String text, String start, String end) {
        List<String> results = scrap(text, start, end);
        if (results.size() == 1) {
            return results.get(0);
        }
        return null;
    }

    public static List<String> scrap(String text, String start, String end) {
        List<String> results = new ArrayList<>();
        int from = 0;
        while (from < text.length()) {
            int indexOfStart = text.indexOf(start, from);
            int indexOfEnd = text.indexOf(end, indexOfStart + start.length());
            if (indexOfStart != -1 && indexOfEnd != -1) {
                results.add(text.substring(indexOfStart + start.length(), indexOfEnd));
                from = indexOfEnd + end.length();
            } else {
                break;
            }
        }
        return results;
    }

    public static void createSymbolicLink(Path source, Path link) throws IOException {
        Path relativeSrc = link.getParent().relativize(source); // relative path of original file from symbolic link
        link.getParent().toFile().mkdirs(); // create the directory hierarchy if any folder is not available
        Files.createSymbolicLink(link, relativeSrc); // create symbolic link.
    }
    
    public static void deleteRecursive(Path pathToBeDeleted) throws IOException
    {
    	Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
 }
