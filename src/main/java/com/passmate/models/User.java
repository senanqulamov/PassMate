package com.passmate.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a user in the PassMate application.
 * Contains user profile information and authentication details.
 */
public class User {
    private final String id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty profileImagePath;
    private final StringProperty createdDate;

    /**
     * Creates a new User with default values.
     */
    public User() {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.profileImagePath = new SimpleStringProperty("");
        this.createdDate = new SimpleStringProperty("");
    }

    /**
     * Creates a new User with the specified details.
     * @param name The user's display name
     * @param email The user's email address
     */
    public User(String name, String email) {
        this();
        this.name.set(name);
        this.email.set(email);
    }

    // ID getter
    public String getId() { return id; }

    // Name property
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Email property
    public StringProperty emailProperty() { return email; }
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    // Profile image path property
    public StringProperty profileImagePathProperty() { return profileImagePath; }
    public String getProfileImagePath() { return profileImagePath.get(); }
    public void setProfileImagePath(String path) { this.profileImagePath.set(path); }

    // Created date property
    public StringProperty createdDateProperty() { return createdDate; }
    public String getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(String date) { this.createdDate.set(date); }
}
