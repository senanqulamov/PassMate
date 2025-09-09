package com.passmate.controllers;

import com.passmate.services.PinService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PinController {
    private static final int MIN_PIN_LEN = 4;

    @FXML private ImageView profileImage;
    @FXML private Label usernameLabel;
    @FXML private Label instructionLabel;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private PinService pinService;
    private Runnable onAuthenticated;
    private boolean setupMode;

    public void initialize() {
        String user = System.getProperty("user.name", "User");
        usernameLabel.setText(user);
        // Submit on Enter
        if (pinField != null) pinField.setOnAction(e -> onSubmit());
        if (confirmField != null) confirmField.setOnAction(e -> onSubmit());
        // Allow only digits in fields
        if (pinField != null) pinField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("\\d*")) pinField.setText(val.replaceAll("[^\\d]", ""));
        });
        if (confirmField != null) confirmField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("\\d*")) confirmField.setText(val.replaceAll("[^\\d]", ""));
        });
    }

    public void init(PinService pinService, Runnable onAuthenticated, Image profilePic) {
        this.pinService = pinService;
        this.onAuthenticated = onAuthenticated;
        if (profilePic != null) profileImage.setImage(profilePic);
        this.setupMode = pinService == null || !pinService.hasPin();
        updateUiForMode();
    }

    private void updateUiForMode() {
        if (setupMode) {
            if (instructionLabel != null) instructionLabel.setText("Create a new PIN for PassMate");
            if (pinField != null) pinField.setPromptText("New PIN");
            if (confirmField != null) {
                confirmField.setPromptText("Confirm PIN");
                confirmField.setVisible(true);
                confirmField.setManaged(true);
            }
        } else {
            if (instructionLabel != null) instructionLabel.setText("Enter your PassMate PIN");
            if (pinField != null) pinField.setPromptText("Enter PIN");
            if (confirmField != null) {
                confirmField.clear();
                confirmField.setVisible(false);
                confirmField.setManaged(false);
            }
        }
        if (errorLabel != null) errorLabel.setText("");
        if (pinField != null) pinField.requestFocus();
    }

    @FXML
    private void onDigit(javafx.event.ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof javafx.scene.control.Button) {
            javafx.scene.control.Button b = (javafx.scene.control.Button) src;
            PasswordField target = targetFieldForAppend();
            target.appendText(b.getText());
        }
    }

    private PasswordField targetFieldForAppend() {
        if (!setupMode || confirmField == null || !confirmField.isVisible()) return pinField;
        String p = pinField.getText() == null ? "" : pinField.getText();
        String c = confirmField.getText() == null ? "" : confirmField.getText();
        return (p.length() <= c.length()) ? pinField : confirmField;
    }

    @FXML
    private void onClear() {
        if (setupMode && confirmField != null && confirmField.isVisible()) {
            pinField.clear();
            confirmField.clear();
        } else {
            pinField.clear();
        }
        errorLabel.setText("");
        pinField.requestFocus();
    }

    @FXML
    private void onSubmit() {
        if (pinService == null) return;
        if (setupMode) {
            char[] a = pinField.getText() == null ? new char[0] : pinField.getText().toCharArray();
            char[] b = confirmField != null && confirmField.getText() != null ? confirmField.getText().toCharArray() : new char[0];
            try {
                if (a.length < MIN_PIN_LEN) {
                    errorLabel.setText("PIN must be at least " + MIN_PIN_LEN + " digits");
                    return;
                }
                if (!java.util.Arrays.equals(a, b)) {
                    errorLabel.setText("PINs do not match");
                    return;
                }
                pinService.setPin(a);
                setupMode = false;
                updateUiForMode();
                if (onAuthenticated != null) onAuthenticated.run();
            } finally {
                java.util.Arrays.fill(a, '\0');
                java.util.Arrays.fill(b, '\0');
            }
        } else {
            char[] pin = pinField.getText() == null ? new char[0] : pinField.getText().toCharArray();
            boolean ok;
            try {
                ok = pinService.validatePin(pin);
            } finally {
                java.util.Arrays.fill(pin, '\0');
            }
            if (ok) {
                if (onAuthenticated != null) onAuthenticated.run();
            } else {
                errorLabel.setText("Invalid PIN");
                pinField.clear();
                pinField.requestFocus();
            }
        }
    }
}
