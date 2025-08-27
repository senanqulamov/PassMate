package com.passmate.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents the user's local vault containing categories and password entries.
 * P1: holds only categories. Password entities will be added in later phases.
 */
public class Vault {
    private final List<Category> categories = new ArrayList<>();

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
        return categories.removeIf(c -> c.getId().equals(id));
    }
}
