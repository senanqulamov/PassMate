package com.passmate.controllers.components;

import com.passmate.models.Category;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CategorySliderController {
    @FXML private HBox container;
    private final ToggleGroup group = new ToggleGroup();
    private Consumer<Category> onSelect;

    public void setOnSelect(Consumer<Category> onSelect) {
        this.onSelect = onSelect;
    }

    public void setCategories(List<Category> categories, UUID activeId) {
        container.getChildren().clear();
        for (Category c : categories) {
            ToggleButton chip = new ToggleButton(c.getName());
            chip.getStyleClass().add("category-chip");
            chip.setUserData(c);
            chip.setToggleGroup(group);
            chip.setOnAction(e -> {
                if (onSelect != null) onSelect.accept(c);
            });
            if (activeId != null && activeId.equals(c.getId())) {
                chip.setSelected(true);
            }
            container.getChildren().add(chip);
        }
        // select first if none selected and list non-empty
        if (group.getSelectedToggle() == null && !categories.isEmpty()) {
            container.getChildren().get(0).requestFocus();
            ((ToggleButton)container.getChildren().get(0)).setSelected(true);
            if (onSelect != null) onSelect.accept((Category) ((ToggleButton)container.getChildren().get(0)).getUserData());
        }
    }
}

