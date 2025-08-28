package com.passmate.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a password entry belonging to a category. The password field is stored encrypted.
 */
public class Password {
    private final UUID id;
    private String title;
    private String username;
    /**
     * Encrypted password payload (base64-encoded). Never store plaintext.
     */
    private String passwordHash;
    private UUID categoryId;

    public Password(String title, String username, String passwordHash, UUID categoryId) {
        this(UUID.randomUUID(), title, username, passwordHash, categoryId);
    }

    public Password(UUID id, String title, String username, String passwordHash, UUID categoryId) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        if (username == null) username = "";
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash cannot be null/blank");
        if (categoryId == null) throw new IllegalArgumentException("categoryId cannot be null");
        this.id = id;
        this.title = title.trim();
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.categoryId = categoryId;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be null/blank");
        this.title = title.trim();
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username == null ? "" : username.trim(); }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash cannot be null/blank");
        this.passwordHash = passwordHash;
    }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("categoryId cannot be null");
        this.categoryId = categoryId;
    }

    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Password that = (Password) o; return id.equals(that.id);}
    @Override public int hashCode() { return Objects.hash(id); }
}

