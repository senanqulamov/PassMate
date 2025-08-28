package com.passmate.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SidebarController {
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton btnVault;
    @FXML private ToggleButton btnFavourites;
    @FXML private ToggleButton btnBin;

    @FXML private TextField categoryInput;
    @FXML private Button addCategoryBtn;

    @FXML private CategoryListController categoryListController;

    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;

    public ToggleGroup getNavGroup() { return navGroup; }
    public ToggleButton getBtnVault() { return btnVault; }
    public ToggleButton getBtnFavourites() { return btnFavourites; }
    public ToggleButton getBtnBin() { return btnBin; }

    public TextField getCategoryInput() { return categoryInput; }
    public Button getAddCategoryBtn() { return addCategoryBtn; }

    public CategoryListController getCategoryListController() { return categoryListController; }

    public void setUsername(String name) { if (userNameLabel != null) userNameLabel.setText(name == null ? "User" : name); }
    public void setProfileImage(Image img) { if (userAvatar != null && img != null) userAvatar.setImage(img); }
}
