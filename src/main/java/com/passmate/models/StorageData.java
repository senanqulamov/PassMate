package com.passmate.models;

import java.util.List;
import java.util.Map;

/**
 * Data model for encrypted storage container.
 * This represents the complete application state that gets encrypted and saved to JSON.
 */
public class StorageData {
    private List<Password> passwords;
    private List<Category> categories;
    private Vault vault;
    private Map<String, Object> settings;
    private String version;
    private long timestamp;

    public StorageData() {
        this.version = "1.0";
        this.timestamp = System.currentTimeMillis();
    }

    public StorageData(List<Password> passwords, List<Category> categories, Vault vault, Map<String, Object> settings) {
        this();
        this.passwords = passwords;
        this.categories = categories;
        this.vault = vault;
        this.settings = settings;
    }

    // Getters and setters
    public List<Password> getPasswords() {
        return passwords;
    }

    public void setPasswords(List<Password> passwords) {
        this.passwords = passwords;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
