package com.passmate.utils;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Minimal toast/notification utility inspired by shadcn/ui toasts.
 */
public final class ToastUtil {
    public enum Type { SUCCESS, ERROR, INFO }

    private ToastUtil() {}

    /**
     * Show a toast overlayed on the given StackPane parent (e.g., contentArea).
     * Auto disappears after a short timeout. Stacks toasts without overlap.
     */
    public static void show(StackPane parent, String message, Type type) {
        if (parent == null) return;
        HBox toast = buildToastNode(message, type);
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        int existing = (int) parent.getChildren().stream()
                .filter(n -> n.getStyleClass().contains("toast"))
                .count();
        double offset = 16 + existing * 68; // approximate height + spacing
        StackPane.setMargin(toast, new Insets(offset, 16, 16, 16));
        parent.getChildren().add(toast);

        // Animations: slide-in + fade-in, wait, fade-out + slide-up
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(140), toast);
        slideIn.setFromY(-10);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(140), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition stay = new FadeTransition(Duration.millis(2000), toast);
        stay.setFromValue(1);
        stay.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), toast);
        slideUp.setFromY(0);
        slideUp.setToY(-10);

        SequentialTransition seq = new SequentialTransition(slideIn, fadeIn, stay, fadeOut, slideUp);
        seq.setOnFinished(e -> parent.getChildren().remove(toast));
        seq.play();
    }

    private static HBox buildToastNode(String message, Type type) {
        Label label = new Label(message);
        label.getStyleClass().add("toast-text");
        label.setWrapText(true);
        label.setMaxWidth(320);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(12, label);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setOpacity(0);
        box.getStyleClass().add("toast");
        switch (type) {
            case SUCCESS -> box.getStyleClass().add("toast-success");
            case ERROR -> box.getStyleClass().add("toast-error");
            case INFO -> box.getStyleClass().add("toast-info");
        }
        return box;
    }
}
