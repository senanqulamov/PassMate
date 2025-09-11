package com.passmate.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Category {
    private final String id;
    private final StringProperty name;
    private final StringProperty iconName;
    private final StringProperty color;

    public Category() {
        this.id = "category_" + System.currentTimeMillis();
        this.name = new SimpleStringProperty("");
        this.iconName = new SimpleStringProperty("folder-personal-icon");
        this.color = new SimpleStringProperty("#666666");
    }

    @JsonCreator
    public Category(@JsonProperty("id") String id,
                    @JsonProperty("name") String name,
                    @JsonProperty("iconName") String iconName,
                    @JsonProperty("color") String color) {
        this.id = id != null ? id : (name != null ? name.toLowerCase().replaceAll("\\s+", "_") : ("category_" + System.currentTimeMillis()));
        this.name = new SimpleStringProperty(name != null ? name : "");
        this.iconName = new SimpleStringProperty(iconName != null ? iconName : "folder-personal-icon");
        this.color = new SimpleStringProperty(color != null ? color : "#666666");
    }

    public Category(String id, String name, String iconName) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.iconName = new SimpleStringProperty(iconName);
        this.color = new SimpleStringProperty("#666666");
    }

    // ID getter
    public String getId() { return id; }

    // Name property
    @JsonIgnore
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Icon name property
    @JsonIgnore
    public StringProperty iconNameProperty() { return iconName; }
    public String getIconName() { return iconName.get(); }
    public void setIconName(String iconName) { this.iconName.set(iconName); }

    // Color property
    @JsonIgnore
    public StringProperty colorProperty() { return color; }
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + getName() + '\'' +
                ", iconName='" + getIconName() + '\'' +
                ", color='" + getColor() + '\'' +
                '}';
    }
}
