package com.passmate.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.passmate.models.Category;
import com.passmate.services.VaultService;
import com.passmate.services.exceptions.DuplicateCategoryException;
import com.passmate.services.exceptions.CategoryNotFoundException;
import com.passmate.utils.ToastUtil;

public class MainController {

    @FXML private HBox windowControls;
    @FXML private VBox sidebar;
    @FXML private Label closeBtn;
    @FXML private Label minimizeBtn;
    @FXML private StackPane contentArea;
    // New FXML elements for categories
    @FXML private TextField categoryInput;
    @FXML private Button addCategoryBtn;
    @FXML private VBox folderList;

    private double xOffset = 0;
    private double yOffset = 0;

    private VaultService vaultService;
    private Category activeCategory;

    public void initialize() {
        setupWindowControls();
        setupCategoryInputs();
    }

    /** DI entry point from App.java */
    public void initData(VaultService vaultService) {
        this.vaultService = vaultService;
        // Seed initial categories if empty (one-time UX sugar)
        if (vaultService.getCategories().isEmpty()) {
            List.of("Social", "Games", "Personal", "Mails", "Work").forEach(name -> {
                if (!vaultService.existsByNameIgnoreCase(name)) {
                    try { vaultService.createCategory(name); } catch (Exception ignored) {}
                }
            });
        }
        renderFolderButtons();
    }

    private void setupCategoryInputs() {
        if (addCategoryBtn != null) {
            addCategoryBtn.setOnAction(e -> handleAddCategory());
        }
        if (categoryInput != null) {
            categoryInput.setOnAction(e -> handleAddCategory());
        }
    }

    private void handleAddCategory() {
        String name = categoryInput.getText();
        if (name == null || name.isBlank()) {
            flashInputError("Enter a folder name");
            ToastUtil.show(contentArea, "Folder name can't be empty", ToastUtil.Type.ERROR);
            return;
        }
        try {
            Category created = vaultService.createCategory(name.trim());
            categoryInput.clear();
            renderFolderButtons();
            // auto-select newly created
            selectCategory(created);
            ToastUtil.show(contentArea, "Folder created", ToastUtil.Type.SUCCESS);
        } catch (DuplicateCategoryException ex) {
            flashInputError("Folder exists");
            ToastUtil.show(contentArea, "Folder already exists", ToastUtil.Type.ERROR);
        } catch (IllegalArgumentException ex) {
            flashInputError("Invalid name");
            ToastUtil.show(contentArea, "Invalid folder name", ToastUtil.Type.ERROR);
        }
    }

    private void flashInputError(String message) {
        if (categoryInput == null) return;
        String oldPrompt = categoryInput.getPromptText();
        categoryInput.clear();
        categoryInput.setPromptText(message);
        categoryInput.getStyleClass().add("error");
        // remove error style shortly after focus
        categoryInput.focusedProperty().addListener((obs, o, n) -> {
            categoryInput.getStyleClass().remove("error");
            categoryInput.setPromptText(oldPrompt == null ? "New folder" : oldPrompt);
        });
    }

    private void renderFolderButtons() {
        if (folderList == null) return;
        folderList.getChildren().clear();
        for (Category c : vaultService.getCategories()) {
            Label btn = createFolderButton(c);
            folderList.getChildren().add(btn);
        }
        if (activeCategory != null) {
            // Restore active style if still present
            folderList.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .map(n -> (Label) n)
                    .filter(l -> {
                        Object userData = l.getUserData();
                        return userData instanceof UUID && ((UUID) userData).equals(activeCategory.getId());
                    })
                    .findFirst()
                    .ifPresent(l -> l.getStyleClass().add("active-folder"));
        }
    }

    private Label createFolderButton(Category category) {
        Label label = new Label(" " + category.getName());
        label.getStyleClass().add("folder-btn");
        label.setUserData(category.getId());

        // Add icon to the button
        FontIcon icon = new FontIcon(getIconLiteralFor(category.getName()));
        icon.setIconSize(15);
        label.setGraphic(icon);

        // Set click event
        label.setOnMouseClicked(e -> selectCategory(category));

        // Context menu for rename/delete
        ContextMenu menu = new ContextMenu();
        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(category.getName());
            dialog.setTitle("Rename Folder");
            dialog.setHeaderText(null);
            dialog.setContentText("New name:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                try {
                    Category updated = vaultService.renameCategory(category.getId(), newName);
                    renderFolderButtons();
                    selectCategory(updated);
                    ToastUtil.show(contentArea, "Folder renamed", ToastUtil.Type.SUCCESS);
                } catch (DuplicateCategoryException ex) {
                    ToastUtil.show(contentArea, "Folder already exists", ToastUtil.Type.ERROR);
                } catch (IllegalArgumentException ex) {
                    ToastUtil.show(contentArea, "Invalid folder name", ToastUtil.Type.ERROR);
                } catch (CategoryNotFoundException ex) {
                    ToastUtil.show(contentArea, "Folder not found", ToastUtil.Type.ERROR);
                }
            });
        });
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> {
            try {
                vaultService.deleteCategory(category.getId());
                if (activeCategory != null && activeCategory.getId().equals(category.getId())) {
                    activeCategory = null;
                    contentArea.getChildren().clear();
                }
                renderFolderButtons();
                ToastUtil.show(contentArea, "Folder deleted", ToastUtil.Type.SUCCESS);
            } catch (CategoryNotFoundException ignored) {
                ToastUtil.show(contentArea, "Folder not found", ToastUtil.Type.ERROR);
            } catch (Exception ignored) {
                ToastUtil.show(contentArea, "Couldn't delete folder", ToastUtil.Type.ERROR);
            }
        });
        menu.getItems().addAll(rename, delete);
        label.setOnContextMenuRequested(e -> menu.show(label, e.getScreenX(), e.getScreenY()));

        // Add hover animation
        setupHoverAnimation(label);

        return label;
    }

    private void selectCategory(Category category) {
        activeCategory = category;
        resetActiveStyles();
        // Find the corresponding label and style it active
        folderList.getChildren().forEach(node -> {
            if (node instanceof Label lbl && lbl.getUserData() instanceof UUID id && id.equals(category.getId())) {
                lbl.getStyleClass().add("active-folder");
            }
        });
        showPasswordList(category.getName());
    }

    private void resetActiveStyles() {
        // Remove "active-folder" style from all folder buttons
        folderList.getChildren().forEach(node -> node.getStyleClass().remove("active-folder"));
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

        // Section header with selected category name
        Label header = new Label(category);
        header.getStyleClass().add("section-title");
        list.getChildren().add(header);

        for (int i = 1; i <= 5; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/PasswordCard.fxml"));
                Node card = loader.load();
                list.getChildren().add(card);
            } catch (IOException e) {
                System.err.println("Failed to load PasswordCard.fxml: " + e.getMessage());
                Label errorLabel = new Label("Failed to load password cards");
                contentArea.getChildren().setAll(errorLabel);
            }
        }

        contentArea.getChildren().setAll(list);
    }
}
