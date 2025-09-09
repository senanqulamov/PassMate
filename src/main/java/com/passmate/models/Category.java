package com.passmate.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.UUID;

public class Category {
    private final String id;
    private final StringProperty name;
    private final StringProperty iconName;
    private final StringProperty color;

    public Category() {
        this.id = UUID.randomUUID().toString();
        this.name = new SimpleStringProperty("");
        this.iconName = new SimpleStringProperty("");
        this.color = new SimpleStringProperty("#666666");
    }

    public Category(String name, String iconName, String color) {
        this.id = UUID.randomUUID().toString();
        this.name = new SimpleStringProperty(name);
        this.iconName = new SimpleStringProperty(iconName);
        this.color = new SimpleStringProperty(color);
    }

    // ID getter
    public String getId() { return id; }

    // Name property
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Icon name property
    public StringProperty iconNameProperty() { return iconName; }
    public String getIconName() { return iconName.get(); }
    public void setIconName(String iconName) { this.iconName.set(iconName); }

    // Color property
    public StringProperty colorProperty() { return color; }
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }
}
