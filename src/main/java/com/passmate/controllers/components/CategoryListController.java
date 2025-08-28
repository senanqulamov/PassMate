package com.passmate.controllers.components;

import com.passmate.models.Category;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Reusable category list component. Renders categories, supports select, rename, delete via handlers.
 */
public class CategoryListController {
    @FXML private VBox listContainer;

    private Consumer<Category> onSelect;
    private BiConsumer<Category, String> onRename; // (category, newName)
    private Consumer<Category> onDelete;
    private UUID activeId;

    public void setHandlers(Consumer<Category> onSelect,
                            BiConsumer<Category, String> onRename,
                            Consumer<Category> onDelete) {
        this.onSelect = onSelect;
        this.onRename = onRename;
        this.onDelete = onDelete;
    }

    public void setCategories(List<Category> categories, UUID activeId) {
        this.activeId = activeId;
        listContainer.getChildren().clear();
        if (categories == null) return;
        for (Category c : categories) {
            Label item = buildItem(c);
            listContainer.getChildren().add(item);
        }
    }

    private Label buildItem(Category category) {
        Label label = new Label(" " + category.getName());
        label.getStyleClass().add("folder-btn");
        label.setPadding(new Insets(8, 10, 8, 10));
        label.setUserData(category.getId());

        FontIcon icon = new FontIcon(getIconLiteralFor(category.getName()));
        icon.setIconSize(15);
        label.setGraphic(icon);

        if (activeId != null && activeId.equals(category.getId())) {
            label.getStyleClass().add("active-folder");
        }

        label.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && onSelect != null) onSelect.accept(category);
        });

        ContextMenu menu = new ContextMenu();
        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> startInlineRename(label, category));
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> { if (onDelete != null) onDelete.accept(category); });
        menu.getItems().addAll(rename, delete);

        // Attach context menu and ensure secondary click shows it reliably
        label.setContextMenu(menu);
        label.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                menu.show(label, e.getScreenX(), e.getScreenY());
            }
        });

        return label;
    }

    private void startInlineRename(Label label, Category category) {
        int idx = listContainer.getChildren().indexOf(label);
        if (idx < 0) return;
        TextField tf = new TextField(category.getName());
        tf.getStyleClass().add("folder-rename-field");
        tf.setPadding(new Insets(6, 8, 6, 8));
        listContainer.getChildren().set(idx, tf);
        tf.requestFocus();
        tf.selectAll();
        Runnable commit = () -> {
            String newName = tf.getText();
            if (newName != null && !newName.isBlank() && onRename != null) {
                onRename.accept(category, newName.trim());
            }
        };
        tf.setOnAction(e -> commit.run());
        tf.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                // cancel
                listContainer.getChildren().set(idx, buildItem(category));
            }
        });
        tf.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                commit.run();
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
}
