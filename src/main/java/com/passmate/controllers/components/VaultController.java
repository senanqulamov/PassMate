package com.passmate.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class VaultController {
    @FXML private Label titleLabel;
    @FXML private Button addBtn;
    @FXML private FlowPane grid;

    public void setTitle(String title) {
        if (titleLabel != null) titleLabel.setText(title);
    }

    public Button getAddBtn() { return addBtn; }
    public FlowPane getGrid() { return grid; }
}

