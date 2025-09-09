package com.passmate.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.UUID;

public class Password {
    private final String id;
    private final StringProperty name;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty website;
    private final StringProperty notes;
    private final StringProperty category;
    private final StringProperty createdBy;
    private final StringProperty createdDate;
    private final StringProperty lastUpdated;
    private final StringProperty iconUrl;
    private final StringProperty passwordHash;
    private final StringProperty categoryId;

    public Password() {
        this.id = UUID.randomUUID().toString();
        this.name = new SimpleStringProperty("");
        this.username = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.website = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty("");
        this.category = new SimpleStringProperty("");
        this.createdBy = new SimpleStringProperty("");
        this.createdDate = new SimpleStringProperty("");
        this.lastUpdated = new SimpleStringProperty("");
        this.iconUrl = new SimpleStringProperty("");
        this.passwordHash = new SimpleStringProperty("");
        this.categoryId = new SimpleStringProperty("");
    }

    public Password(String name, String username, String password, String website) {
        this();
        this.name.set(name);
        this.username.set(username);
        this.password.set(password);
        this.website.set(website);
    }

    public Password(String name, String username, String password, String website, String notes, String categoryId) {
        this();
        this.name.set(name);
        this.username.set(username);
        this.password.set(password);
        this.website.set(website);
        this.notes.set(notes);
        this.categoryId.set(categoryId);
    }

    // ID getter
    public String getId() { return id; }

    // Title methods (alias for name to maintain compatibility)
    public String getTitle() { return getName(); }
    public void setTitle(String title) { setName(title); }
    public StringProperty titleProperty() { return nameProperty(); }

    // Password hash methods
    public String getPasswordHash() { return passwordHash.get(); }
    public void setPasswordHash(String hash) { this.passwordHash.set(hash); }
    public StringProperty passwordHashProperty() { return passwordHash; }

    // Category ID methods
    public String getCategoryId() { return categoryId.get(); }
    public void setCategoryId(String categoryId) { this.categoryId.set(categoryId); }
    public StringProperty categoryIdProperty() { return categoryId; }

    // Name property
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Username property
    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }

    // Password property
    public StringProperty passwordProperty() { return password; }
    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }

    // Website property
    public StringProperty websiteProperty() { return website; }
    public String getWebsite() { return website.get(); }
    public void setWebsite(String website) { this.website.set(website); }

    // Notes property
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }

    // Category property
    public StringProperty categoryProperty() { return category; }
    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    // Created by property
    public StringProperty createdByProperty() { return createdBy; }
    public String getCreatedBy() { return createdBy.get(); }
    public void setCreatedBy(String createdBy) { this.createdBy.set(createdBy); }

    // Created date property
    public StringProperty createdDateProperty() { return createdDate; }
    public String getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(String createdDate) { this.createdDate.set(createdDate); }

    // Last updated property
    public StringProperty lastUpdatedProperty() { return lastUpdated; }
    public String getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated.set(lastUpdated); }

    // Icon URL property
    public StringProperty iconUrlProperty() { return iconUrl; }
    public String getIconUrl() { return iconUrl.get(); }
    public void setIconUrl(String iconUrl) { this.iconUrl.set(iconUrl); }
}
