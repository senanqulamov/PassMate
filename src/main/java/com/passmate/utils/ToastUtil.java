package com.passmate.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastUtil {

    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    public static void showToast(Stage owner, String message, Type type) {
        Popup popup = new Popup();

        VBox content = new VBox();
        content.getStyleClass().add("toast");
        content.getStyleClass().add("toast-" + type.name().toLowerCase());
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(300);
        content.setSpacing(5);

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);

        content.getChildren().add(messageLabel);
        popup.getContent().add(content);

        // Position the toast
        popup.setAutoHide(true);
        popup.show(owner);

        // Center the popup
        double centerX = owner.getX() + owner.getWidth() / 2 - 150;
        double centerY = owner.getY() + 100;
        popup.setX(centerX);
        popup.setY(centerY);

        // Add fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), content);
        slideIn.setFromY(-50);
        slideIn.setToY(0);

        fadeIn.play();
        slideIn.play();

        // Auto hide after 3 seconds
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), content);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> popup.hide());
        fadeOut.play();
    }

    public static void showSuccess(Stage owner, String message) {
        showToast(owner, message, Type.SUCCESS);
    }

    public static void showError(Stage owner, String message) {
        showToast(owner, message, Type.ERROR);
    }

    public static void showInfo(Stage owner, String message) {
        showToast(owner, message, Type.INFO);
    }

    public static void showWarning(Stage owner, String message) {
        showToast(owner, message, Type.WARNING);
    }
}
