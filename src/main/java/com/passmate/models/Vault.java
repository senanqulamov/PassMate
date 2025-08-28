package com.passmate.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the user's local vault containing categories and password entries.
 */
public class Vault {
    private final List<Category> categories = new ArrayList<>();
    private final List<Password> passwords = new ArrayList<>();

    /**
     * An unmodifiable snapshot of categories.
     */
    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Add a category to the vault.
     * @param category non-null category
     */
    public void addCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("category cannot be null");
        categories.add(category);
    }

    /**
     * Remove a category by id.
     * @param id category id
     * @return true if removed
     */
    public boolean removeCategory(UUID id) {
        // Also remove passwords belonging to this category
        passwords.removeIf(p -> p.getCategoryId().equals(id));
        return categories.removeIf(c -> c.getId().equals(id));
    }

    public List<Password> getPasswords() {
        return Collections.unmodifiableList(passwords);
    }

    public List<Password> getPasswordsByCategory(UUID categoryId) {
        return passwords.stream().filter(p -> p.getCategoryId().equals(categoryId)).collect(Collectors.toList());
    }

    public void addPassword(Password password) {
        if (password == null) throw new IllegalArgumentException("password cannot be null");
        passwords.add(password);
    }

    public boolean removePassword(UUID id) {
        return passwords.removeIf(p -> p.getId().equals(id));
    }

    public boolean updatePassword(Password updated) {
        if (updated == null) return false;
        for (int i = 0; i < passwords.size(); i++) {
            Password p = passwords.get(i);
            if (p.getId().equals(updated.getId())) {
                // Update fields in-place
                p.setTitle(updated.getTitle());
                p.setUsername(updated.getUsername());
                p.setPasswordHash(updated.getPasswordHash());
                p.setCategoryId(updated.getCategoryId());
                return true;
            }
        }
        return false;
    }
}
