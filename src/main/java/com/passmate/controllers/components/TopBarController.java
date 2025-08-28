package com.passmate.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TopBarController {
    @FXML private Button menuBtn;
    @FXML private TextField searchField;
    @FXML private Label closeBtn;
    @FXML private Label minimizeBtn;

    public Button getMenuBtn() { return menuBtn; }
    public TextField getSearchField() { return searchField; }
    public Label getCloseBtn() { return closeBtn; }
    public Label getMinimizeBtn() { return minimizeBtn; }
}

