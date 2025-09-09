package com.passmate.services;

import com.passmate.models.Vault;
import com.passmate.models.Category;
import com.passmate.models.Password;
import javafx.collections.ObservableList;

/**
 * Service interface for vault management operations.
 * Handles vault creation, category management, and password organization.
 */
public interface VaultService {

    /**
     * Creates a new vault for the given user.
     * @param vaultName The name of the vault
     * @param ownerName The owner of the vault
     * @return The created vault
     */
    Vault createVault(String vaultName, String ownerName);

    /**
     * Loads an existing vault from storage.
     * @param vaultPath The path to the vault file
     * @return The loaded vault or null if not found
     */
    Vault loadVault(String vaultPath);

    /**
     * Saves the vault to storage.
     * @param vault The vault to save
     * @param vaultPath The path where to save the vault
     * @return true if save was successful, false otherwise
     */
    boolean saveVault(Vault vault, String vaultPath);

    /**
     * Adds a new category to the vault.
     * @param vault The target vault
     * @param category The category to add
     */
    void addCategory(Vault vault, Category category);

    /**
     * Removes a category from the vault.
     * @param vault The target vault
     * @param categoryId The ID of the category to remove
     */
    void removeCategory(Vault vault, String categoryId);

    /**
     * Updates an existing category in the vault.
     * @param vault The target vault
     * @param category The updated category
     */
    void updateCategory(Vault vault, Category category);

    /**
     * Gets all categories in the vault.
     * @param vault The target vault
     * @return Observable list of categories
     */
    ObservableList<Category> getCategories(Vault vault);

    /**
     * Gets all passwords in a specific category.
     * @param vault The target vault
     * @param categoryId The category ID
     * @return Observable list of passwords in the category
     */
    ObservableList<Password> getPasswordsByCategory(Vault vault, String categoryId);
}
