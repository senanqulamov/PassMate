package com.passmate.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private HBox windowControls;
    @FXML private VBox sidebar;
    @FXML private Label closeBtn;
    @FXML private Label minimizeBtn;
    @FXML private StackPane contentArea;

    private double xOffset = 0;
    private double yOffset = 0;

    // Predefined categories
    private final List<String> categories = List.of("Social", "Games", "Personal", "Mails", "Work");

    public void initialize() {
        setupWindowControls();

        // Create folder buttons dynamically
        categories.forEach(category -> sidebar.getChildren().add(createFolderButton(category)));
    }

    private Label createFolderButton(String category) {
        Label label = new Label(" " + category);
        label.getStyleClass().add("folder-btn");

        // Add icon to the button
        FontIcon icon = new FontIcon(getIconLiteralFor(category));
        icon.setIconSize(15);
        label.setGraphic(icon);

        // Set click event
        label.setOnMouseClicked(event -> {
            System.out.println("Selected: " + category);
            resetActiveStyles();
            label.getStyleClass().add("active-folder");
            showPasswordList(category);
        });

        // Add hover animation
        setupHoverAnimation(label);

        return label;
    }

    private void resetActiveStyles() {
        // Remove "active-folder" style from all folder buttons
        sidebar.getChildren().forEach(node -> node.getStyleClass().remove("active-folder"));
    }

    private void setupHoverAnimation(Label label) {
        label.setOnMouseEntered(e -> {
            // Translate and scale animations on hover
            TranslateTransition translate = new TranslateTransition(Duration.millis(150), label);
            translate.setToX(3);
            translate.play();

            if (label.getGraphic() instanceof FontIcon icon) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), icon);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();
            }
        });

        label.setOnMouseExited(e -> {
            // Reset translate and scale animations
            TranslateTransition translate = new TranslateTransition(Duration.millis(150), label);
            translate.setToX(0);
            translate.play();

            if (label.getGraphic() instanceof FontIcon icon) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), icon);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            }
        });
    }

    private String getIconLiteralFor(String category) {
        return switch (category.toLowerCase()) {
            case "social" -> "fas-users";
            case "games" -> "fas-gamepad";
            case "personal" -> "fas-user";
            case "mails" -> "fas-envelope";
            case "work" -> "fas-briefcase";
            default -> "fas-folder";
        };
    }

    private void setupWindowControls() {
        // Close button functionality
        closeBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });

        // Minimize button functionality
        minimizeBtn.setOnMouseClicked(event -> {
            Stage stage = (Stage) minimizeBtn.getScene().getWindow();
            stage.setIconified(true);
        });
    }

    public void makeWindowDraggable(Scene scene, Stage stage) {
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void showPasswordList(String category) {
        VBox list = new VBox(10);
        list.setPadding(new Insets(20, 0, 0, 20));

        for (int i = 1; i <= 5; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/PasswordCard.fxml"));
                Node card = loader.load();
                list.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
                Label errorLabel = new Label("Failed to load password cards");
                contentArea.getChildren().setAll(errorLabel);
            }
        }

        contentArea.getChildren().setAll(list);
    }
}