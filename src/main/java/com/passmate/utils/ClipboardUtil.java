package com.passmate.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for secure clipboard operations.
 * Provides password copying with automatic clearing for security.
 */
public class ClipboardUtil {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ClipboardCleaner");
        t.setDaemon(true);
        return t;
    });

    /**
     * Copies text to clipboard immediately.
     * @param text The text to copy
     */
    public static void copyToClipboard(String text) {
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
        });
    }

    /**
     * Copies password to clipboard with automatic clearing after timeout.
     * This provides enhanced security by ensuring passwords don't remain in clipboard.
     * @param password The password to copy
     * @param timeoutSeconds Time in seconds before clipboard is cleared
     */
    public static void copyPasswordWithTimeout(String password, int timeoutSeconds) {
        // Never log plain text passwords
        System.out.println("Password copied to clipboard (will clear in " + timeoutSeconds + "s)");

        copyToClipboard(password);

        // Schedule clipboard clearing for security
        scheduler.schedule(() -> {
            Platform.runLater(() -> {
                try {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    String currentContent = clipboard.getString();

                    // Only clear if clipboard still contains our password
                    if (password.equals(currentContent)) {
                        ClipboardContent emptyContent = new ClipboardContent();
                        emptyContent.putString("");
                        clipboard.setContent(emptyContent);
                        System.out.println("Clipboard cleared for security");
                    }
                } catch (Exception e) {
                    // Silently handle clipboard access issues
                }
            });
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Copies username to clipboard.
     * @param username The username to copy
     */
    public static void copyUsername(String username) {
        copyToClipboard(username);
        System.out.println("Username copied to clipboard");
    }

    /**
     * Copies website URL to clipboard.
     * @param website The website URL to copy
     */
    public static void copyWebsite(String website) {
        copyToClipboard(website);
        System.out.println("Website URL copied to clipboard");
    }

    /**
     * Shuts down the clipboard cleaner scheduler.
     * Should be called on application exit.
     */
    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
