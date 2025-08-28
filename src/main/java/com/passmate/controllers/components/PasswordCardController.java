package com.passmate.controllers.components;

import com.passmate.models.Password;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PasswordCardController {
    @FXML private Label title;
    @FXML private Label username;
    @FXML private Label masked;
    @FXML private Button copyBtn;
    @FXML private Button viewBtn;

    private Password model;
    private Runnable onCopy;
    private Runnable onView;

    public void setModel(Password model) {
        this.model = model;
        title.setText(model.getTitle());
        username.setText(model.getUsername());
        // masked preview for visual hierarchy
        masked.setText("••••••••");
    }

    public void setHandlers(Runnable onCopy, Runnable onView) {
        this.onCopy = onCopy;
        this.onView = onView;
        copyBtn.setOnAction(e -> { if (this.onCopy != null) this.onCopy.run(); });
        viewBtn.setOnAction(e -> { if (this.onView != null) this.onView.run(); });
    }
}
