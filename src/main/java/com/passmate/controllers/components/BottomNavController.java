package com.passmate.controllers.components;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class BottomNavController {
    @FXML private ToggleButton homeBtn;
    @FXML private ToggleButton vaultBtn;
    @FXML private ToggleButton settingsBtn;

    private final ToggleGroup group = new ToggleGroup();

    private Runnable onHome;
    private Runnable onVault;
    private Runnable onSettings;

    public void init() {
        homeBtn.setToggleGroup(group);
        vaultBtn.setToggleGroup(group);
        settingsBtn.setToggleGroup(group);
        homeBtn.setOnAction(e -> { if (onHome != null) onHome.run(); });
        vaultBtn.setOnAction(e -> { if (onVault != null) onVault.run(); });
        settingsBtn.setOnAction(e -> { if (onSettings != null) onSettings.run(); });
    }

    public void setOnHome(Runnable onHome) { this.onHome = onHome; }
    public void setOnVault(Runnable onVault) { this.onVault = onVault; }
    public void setOnSettings(Runnable onSettings) { this.onSettings = onSettings; }
}

