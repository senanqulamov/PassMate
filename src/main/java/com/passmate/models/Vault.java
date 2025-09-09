package com.passmate.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Vault {
    private final StringProperty name;
    private final StringProperty owner;
    private final ObservableList<Password> passwords;
    private final ObservableList<Category> categories;

    public Vault() {
        this.name = new SimpleStringProperty("");
        this.owner = new SimpleStringProperty("");
        this.passwords = FXCollections.observableArrayList();
        this.categories = FXCollections.observableArrayList();
    }

    public Vault(String name, String owner) {
        this();
        this.name.set(name);
        this.owner.set(owner);
    }

    // Name property
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Owner property
    public StringProperty ownerProperty() { return owner; }
    public String getOwner() { return owner.get(); }
    public void setOwner(String owner) { this.owner.set(owner); }

    // Passwords list
    public ObservableList<Password> getPasswords() { return passwords; }

    // Categories list
    public ObservableList<Category> getCategories() { return categories; }

    // Methods for existing code compatibility
    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(String categoryId) {
        categories.removeIf(cat -> categoryId.equals(cat.getId()));
    }

    public void addPassword(Password password) {
        passwords.add(password);
    }

    public void removePassword(String passwordId) {
        passwords.removeIf(pass -> passwordId.equals(pass.getId()));
    }

    public void updatePassword(Password password) {
        for (int i = 0; i < passwords.size(); i++) {
            if (passwords.get(i).getId().equals(password.getId())) {
                passwords.set(i, password);
                break;
            }
        }
    }

    public ObservableList<Password> getPasswordsByCategory(String categoryId) {
        return passwords.filtered(pass -> categoryId.equals(pass.getCategoryId()));
    }
}
