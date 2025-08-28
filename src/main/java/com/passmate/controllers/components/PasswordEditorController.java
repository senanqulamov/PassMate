package com.passmate.controllers.components;

import com.passmate.models.Password;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PasswordEditorController {
    @FXML private VBox root; // bind to root of FXML
    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField masterKeyField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    public enum Mode { CREATE, EDIT }

    private Mode mode = Mode.CREATE;
    private Password model;

    private TriConsumer<String, String, String> onCreate; // (title, username, plaintext)
    private QuadConsumer<Password, String, String, String> onUpdate; // (model, title, username, newPlain)
    private Runnable onCancel;

    public void initialize() {
        if (cancelBtn != null) cancelBtn.setOnAction(e -> { if (onCancel != null) onCancel.run(); });
        if (saveBtn != null) saveBtn.setOnAction(e -> handleSave());
    }

    public void setHandlers(TriConsumer<String,String,String> onCreate,
                            QuadConsumer<Password,String,String,String> onUpdate,
                            Runnable onCancel) {
        this.onCreate = onCreate;
        this.onUpdate = onUpdate;
        this.onCancel = onCancel;
    }

    public void editNew() {
        mode = Mode.CREATE;
        model = null;
        titleLabel.setText("Create password");
        nameField.clear();
        usernameField.clear();
        passwordField.clear();
        masterKeyField.clear();
        setVisible(true);
    }

    public void editExisting(Password p) {
        mode = Mode.EDIT;
        model = p;
        titleLabel.setText("Edit password");
        nameField.setText(p.getTitle());
        usernameField.setText(p.getUsername());
        passwordField.clear();
        masterKeyField.clear();
        setVisible(true);
    }

    public void setVisible(boolean v) {
        if (root != null) { root.setVisible(v); root.setManaged(v); }
    }

    public char[] getMasterKey() {
        String v = masterKeyField == null ? "" : masterKeyField.getText();
        return v == null ? new char[0] : v.toCharArray();
    }

    private void handleSave() {
        String title = nameField.getText();
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if (mode == Mode.CREATE) {
            if (onCreate != null) onCreate.accept(title, user, pass);
        } else if (mode == Mode.EDIT) {
            if (onUpdate != null) onUpdate.accept(model, title, user, pass.isBlank() ? null : pass);
        }
    }

    // Simple functional interfaces for 3 and 4-arg callbacks
    @FunctionalInterface public interface TriConsumer<A,B,C> { void accept(A a, B b, C c); }
    @FunctionalInterface public interface QuadConsumer<A,B,C,D> { void accept(A a, B b, C c, D d); }
}
