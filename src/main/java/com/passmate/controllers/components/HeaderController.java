package com.passmate.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class HeaderController {
    @FXML private ImageView profileImage;
    @FXML private Label usernameLabel;
    @FXML private Button addBtn;

    public void initialize() {
        // Circular clip for avatar image (36x36)
        if (profileImage != null) {
            Circle clip = new Circle(18, 18, 18);
            profileImage.setClip(clip);
        }
    }

    public void setUsername(String username) {
        if (usernameLabel != null) usernameLabel.setText(username);
    }

    public void setProfileImage(Image image) {
        if (profileImage == null) return;
        if (image != null) {
            profileImage.setImage(image);
        } else {
            // Fallback icon
            try {
                Image fallback = new Image(getClass().getResourceAsStream("/icons/PassMate.png"), 36, 36, true, true);
                profileImage.setImage(fallback);
            } catch (Exception ignored) { }
        }
    }

    public Button getAddBtn() { return addBtn; }
}
