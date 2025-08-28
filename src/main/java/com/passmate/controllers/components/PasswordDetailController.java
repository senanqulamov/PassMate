package com.passmate.controllers.components;

import com.passmate.models.Password;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PasswordDetailController {
    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField masterKeyField;
    @FXML private Button copyBtn;
    @FXML private Button editBtn;
    @FXML private Button dupBtn;
    @FXML private Button delBtn;

    private Password model;
    private BiConsumer<Password,char[]> onCopy;
    private Consumer<Password> onEdit;
    private Consumer<Password> onDuplicate;
    private Consumer<Password> onDelete;

    public void initialize() {
        if (copyBtn != null) copyBtn.setOnAction(e -> { if (onCopy != null && model != null) onCopy.accept(model, getMasterKey()); });
        if (editBtn != null) editBtn.setOnAction(e -> { if (onEdit != null && model != null) onEdit.accept(model); });
        if (dupBtn != null) dupBtn.setOnAction(e -> { if (onDuplicate != null && model != null) onDuplicate.accept(model); });
        if (delBtn != null) delBtn.setOnAction(e -> { if (onDelete != null && model != null) onDelete.accept(model); });
    }

    public void setHandlers(BiConsumer<Password,char[]> onCopy,
                            Consumer<Password> onEdit,
                            Consumer<Password> onDuplicate,
                            Consumer<Password> onDelete) {
        this.onCopy = onCopy;
        this.onEdit = onEdit;
        this.onDuplicate = onDuplicate;
        this.onDelete = onDelete;
    }

    public void setModel(Password p) {
        this.model = p;
        if (p == null) {
            titleLabel.setText("No selection");
            nameField.setText("");
            usernameField.setText("");
        } else {
            titleLabel.setText(p.getTitle());
            nameField.setText(p.getTitle());
            usernameField.setText(p.getUsername());
        }
        if (masterKeyField != null) masterKeyField.clear();
    }

    private char[] getMasterKey() {
        String v = masterKeyField == null ? "" : masterKeyField.getText();
        return v == null ? new char[0] : v.toCharArray();
    }
}

