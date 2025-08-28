package com.passmate.controllers.components;

import com.passmate.models.Password;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PasswordListController {
    @FXML private TextField searchField;
    @FXML private ListView<Password> listView;
    @FXML private Button createBtn;

    private Consumer<String> onSearch;
    private Consumer<Password> onSelect;
    private Runnable onCreate;
    private Consumer<Password> onEdit;
    private Consumer<Password> onDuplicate;
    private Consumer<Password> onDelete;
    private final List<Password> items = new ArrayList<>();
    private UUID activeId;

    public void initialize() {
        listView.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Password item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("vault-item");
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                getStyleClass().add("vault-item");
                // Icon
                Circle dot = new Circle(16, pickColor(item));
                dot.getStyleClass().add("item-icon");
                // Text block
                Label primary = new Label(item.getTitle());
                primary.getStyleClass().add("item-primary");
                Label secondary = new Label(item.getUsername());
                secondary.getStyleClass().add("item-secondary");
                VBox textBox = new VBox(2, primary, secondary);
                textBox.getStyleClass().add("item-text");
                // Action button (copy preview icon placeholder) hidden until hover
                Button action = new Button("⋯");
                action.getStyleClass().addAll("item-action","btn-ghost");
                action.setOnAction(e -> { if (onEdit != null) onEdit.accept(item); });
                HBox container = new HBox(12, dot, textBox, action);
                container.getStyleClass().add("cell-container");
                HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);
                setText(null);
                setGraphic(container);
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null && onSelect != null) onSelect.accept(n);
        });
        listView.setContextMenu(buildContextMenu());
        searchField.textProperty().addListener((obs, old, val) -> { if (onSearch != null) onSearch.accept(val == null ? "" : val.trim().toLowerCase()); });
        if (createBtn != null) createBtn.setOnAction(e -> { if (onCreate != null) onCreate.run(); });
    }

    private Color pickColor(Password p) {
        int h = Math.abs(p.getTitle().hashCode());
        double hue = (h % 360);
        return Color.hsb(hue, 0.55, 0.85);
    }

    private ContextMenu buildContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> { Password p = listView.getSelectionModel().getSelectedItem(); if (p != null && onEdit != null) onEdit.accept(p); });
        MenuItem dup = new MenuItem("Duplicate");
        dup.setOnAction(e -> { Password p = listView.getSelectionModel().getSelectedItem(); if (p != null && onDuplicate != null) onDuplicate.accept(p); });
        MenuItem del = new MenuItem("Delete");
        del.setOnAction(e -> { Password p = listView.getSelectionModel().getSelectedItem(); if (p != null && onDelete != null) onDelete.accept(p); });
        menu.getItems().addAll(edit, dup, del);
        return menu;
    }

    public void setHandlers(Consumer<String> onSearch,
                            Consumer<Password> onSelect,
                            Runnable onCreate,
                            Consumer<Password> onEdit,
                            Consumer<Password> onDuplicate,
                            Consumer<Password> onDelete) {
        this.onSearch = onSearch;
        this.onSelect = onSelect;
        this.onCreate = onCreate;
        this.onEdit = onEdit;
        this.onDuplicate = onDuplicate;
        this.onDelete = onDelete;
    }

    public void setItems(List<Password> list, UUID selectedId) {
        items.clear();
        if (list != null) items.addAll(list);
        listView.getItems().setAll(items);
        if (selectedId != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(selectedId)) {
                    listView.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }
}
