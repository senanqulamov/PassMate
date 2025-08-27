package com.passmate.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a logical folder/category that groups passwords.
 */
public class Category {
    private final UUID id;
    private String name;

    /**
     * Create a Category with a generated id.
     * @param name human-friendly name. Must be non-blank.
     */
    public Category(String name) {
        this(UUID.randomUUID(), name);
    }

    /**
     * Create a Category with provided id and name.
     * @param id unique identifier
     * @param name human-friendly name. Must be non-blank.
     */
    public Category(UUID id, String name) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be null/blank");
        this.id = id;
        this.name = name.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be null/blank");
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}

