package com.passmate.services.impl;

import com.passmate.models.Vault;
import com.passmate.models.Category;
import com.passmate.models.Password;
import com.passmate.services.VaultService;
import com.passmate.services.EncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of VaultService for managing vaults and categories.
 */
public class VaultServiceImpl implements VaultService {

    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    public VaultServiceImpl(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Vault createVault(String vaultName, String ownerName) {
        Vault vault = new Vault(vaultName, ownerName);

        // Add default categories
        vault.addCategory(new Category("Personal", "user-icon", "#3366ff"));
        vault.addCategory(new Category("Work", "briefcase-icon", "#28a745"));
        vault.addCategory(new Category("Social", "users-icon", "#e4405f"));
        vault.addCategory(new Category("Games", "gamepad-icon", "#28a745"));

        return vault;
    }

    @Override
    public Vault loadVault(String vaultPath) {
        try {
            Path path = Paths.get(vaultPath);
            if (!Files.exists(path)) {
                return null;
            }

            String jsonContent = Files.readString(path);
            // In a real implementation, this would be encrypted
            // For now, we'll return a default vault with sample data
            return createVault("My Vault", "Aron Vane");

        } catch (Exception e) {
            System.err.println("Failed to load vault: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean saveVault(Vault vault, String vaultPath) {
        try {
            Path path = Paths.get(vaultPath);
            Files.createDirectories(path.getParent());

            // In a real implementation, this would be encrypted
            String jsonContent = objectMapper.writeValueAsString(vault);
            Files.writeString(path, jsonContent);

            return true;
        } catch (Exception e) {
            System.err.println("Failed to save vault: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addCategory(Vault vault, Category category) {
        vault.addCategory(category);
    }

    @Override
    public void removeCategory(Vault vault, String categoryId) {
        vault.removeCategory(categoryId);
    }

    @Override
    public void updateCategory(Vault vault, Category category) {
        // Remove old and add updated
        vault.removeCategory(category.getId());
        vault.addCategory(category);
    }

    @Override
    public ObservableList<Category> getCategories(Vault vault) {
        return vault.getCategories();
    }

    @Override
    public ObservableList<Password> getPasswordsByCategory(Vault vault, String categoryId) {
        return vault.getPasswordsByCategory(categoryId);
    }
}
