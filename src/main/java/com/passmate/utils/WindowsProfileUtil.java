package com.passmate.utils;

import javafx.scene.image.Image;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WindowsProfileUtil {
    /** Try to fetch current user's profile picture. Returns null if not found. */
    public static Image getUserImage() {
        if (!isWindows()) return null;
        // Common locations
        String appData = System.getenv("APPDATA");
        String programData = System.getenv("PROGRAMDATA");
        String publicPics = System.getenv("PUBLIC");
        // Candidate directories and filename patterns
        Path accPics = appData == null ? null : Path.of(appData, "Microsoft", "Windows", "AccountPictures");
        Path progUserPics = programData == null ? null : Path.of(programData, "Microsoft", "User Account Pictures");
        Path pubAccPics = publicPics == null ? null : Path.of(publicPics, "AccountPictures");

        for (Path dir : List.of(accPics, pubAccPics, progUserPics)) {
            if (dir == null || !Files.isDirectory(dir)) continue;
            try {
                var stream = Files.list(dir);
                var imgPath = stream
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
                        })
                        .findFirst()
                        .orElse(null);
                if (imgPath != null) {
                    return new Image(imgPath.toUri().toString(), 64, 64, true, true);
                }
            } catch (Exception ignored) { }
        }
        return null;
    }

    public static String getUsername() {
        return System.getProperty("user.name", "User");
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }
}

