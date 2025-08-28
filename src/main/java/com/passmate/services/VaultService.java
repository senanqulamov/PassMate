package com.passmate.services;

import com.passmate.models.Category;
import com.passmate.models.Password;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for managing the Vault's categories and password groups.
 */
public interface VaultService {
    // Category operations
    /**
     * @return all categories ordered by name ascending.
     */
    List<Category> getCategories();

    /**
     * Create and add a category.
     * @param name non-blank name
     * @return created Category
     * @throws IllegalArgumentException if name blank
     * @throws com.passmate.services.exceptions.DuplicateCategoryException if name already exists (case-insensitive)
     */
    Category createCategory(String name);

    /**
     * Rename a category.
     * @param id category id
     * @param newName non-blank new name
     * @return updated Category
     * @throws com.passmate.services.exceptions.CategoryNotFoundException if id doesn't exist
     * @throws com.passmate.services.exceptions.DuplicateCategoryException if name already exists (case-insensitive)
     * @throws IllegalArgumentException if newName blank
     */
    Category renameCategory(UUID id, String newName);

    /**
     * Delete a category by id.
     * @param id category id
     * @throws com.passmate.services.exceptions.CategoryNotFoundException if id doesn't exist
     */
    void deleteCategory(UUID id);

    /**
     * @param name name to check
     * @return true if a category exists with the given name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * @param id category id
     * @return category if present
     */
    Optional<Category> findById(UUID id);

    // Password operations
    /** Add password to vault and persist. */
    void addPassword(Password password);
    /** Delete password by id and persist. */
    void deletePassword(UUID id);
    /** List passwords for a category. */
    List<Password> getPasswordsByCategory(UUID categoryId);

    /** Find a password by id. */
    Optional<Password> findPasswordById(UUID id);
    /** Update a password entry and persist. */
    void updatePassword(Password updated);
}
