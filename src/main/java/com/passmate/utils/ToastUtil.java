package com.passmate.utils;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
        HBox toast = loadToast(message, type);
        StackPane.setAlignment(toast, Pos.BOTTOM_LEFT);
        int existing = (int) parent.getChildren().stream()
                .filter(n -> n.getStyleClass().contains("toast"))
                .count();
        double offset = 12 + existing * 56;
        StackPane.setMargin(toast, new Insets(12, 12, offset, 12));
        parent.getChildren().add(toast);

        // Animations
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(120), toast);
        slideIn.setFromY(-8);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition stay = new FadeTransition(Duration.millis(1800), toast);
        stay.setFromValue(1);
        stay.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(160), toast);
        slideUp.setFromY(0);
        slideUp.setToY(-8);

        SequentialTransition seq = new SequentialTransition(slideIn, fadeIn, stay, fadeOut, slideUp);
        seq.setOnFinished(e -> parent.getChildren().remove(toast));
        seq.play();
    }

    private static HBox loadToast(String message, Type type) {
        try {
            FXMLLoader loader = new FXMLLoader(ToastUtil.class.getResource("/views/components/Toast.fxml"));
            HBox root = loader.load();
            Label msg = (Label) root.lookup("#message");
            if (msg != null) msg.setText(message);
            switch (type) {
                case SUCCESS -> root.getStyleClass().add("toast-success");
                case ERROR -> root.getStyleClass().add("toast-error");
                case INFO -> root.getStyleClass().add("toast-info");
            }
            root.setOpacity(0);
            return root;
        } catch (Exception e) {
            // Fallback simple toast if FXML fails
            HBox box = new HBox();
            box.getStyleClass().add("toast");
            Label lbl = new Label(message);
            lbl.getStyleClass().add("toast-text");
            box.getChildren().add(lbl);
            box.setOpacity(0);
            return box;
        }
    }
}
