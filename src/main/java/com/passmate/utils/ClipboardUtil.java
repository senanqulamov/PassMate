package com.passmate.utils;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Clipboard helper that auto-clears sensitive content after a timeout.
 */
public final class ClipboardUtil {
    private ClipboardUtil() {}

    /**
     * Copy text to system clipboard and clear it after timeoutMs.
     */
    public static void copyAndClear(String text, long timeoutMs) {
        if (text == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        if (timeoutMs > 0) {
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override public void run() {
                    Platform.runLater(() -> {
                        Clipboard.getSystemClipboard().clear();
                    });
                }
            }, timeoutMs);
        }
    }
}

