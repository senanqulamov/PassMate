package com.passmate.services;

import com.passmate.models.Password;
import com.passmate.models.Category;
import com.passmate.models.Vault;
import com.passmate.services.exceptions.CryptoException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for encrypted local storage of all application data.
 * Handles saving and loading of passwords, categories, vaults, and settings to/from encrypted JSON files.
 */
public interface StorageService {

    /**
     * Initialize the storage service with master key.
     * @param masterKey The master key for encryption/decryption
     * @throws CryptoException If initialization fails
     */
    void initialize(char[] masterKey) throws CryptoException;

    /**
     * Save all passwords to encrypted storage.
     * @param passwords List of passwords to save
     * @throws CryptoException If encryption/saving fails
     */
    void savePasswords(List<Password> passwords) throws CryptoException;

    /**
     * Load all passwords from encrypted storage.
     * @return List of decrypted passwords
     * @throws CryptoException If decryption/loading fails
     */
    List<Password> loadPasswords() throws CryptoException;

    /**
     * Save all categories to encrypted storage.
     * @param categories List of categories to save
     * @throws CryptoException If encryption/saving fails
     */
    void saveCategories(List<Category> categories) throws CryptoException;

    /**
     * Load all categories from encrypted storage.
     * @return List of decrypted categories
     * @throws CryptoException If decryption/loading fails
     */
    List<Category> loadCategories() throws CryptoException;

    /**
     * Save vault configuration to encrypted storage.
     * @param vault Vault to save
     * @throws CryptoException If encryption/saving fails
     */
    void saveVault(Vault vault) throws CryptoException;

    /**
     * Load vault configuration from encrypted storage.
     * @return Decrypted vault or null if not found
     * @throws CryptoException If decryption/loading fails
     */
    Vault loadVault() throws CryptoException;

    /**
     * Save application settings to encrypted storage.
     * @param settings Map of settings to save
     * @throws CryptoException If encryption/saving fails
     */
    void saveSettings(Map<String, Object> settings) throws CryptoException;

    /**
     * Load application settings from encrypted storage.
     * @return Map of decrypted settings
     * @throws CryptoException If decryption/loading fails
     */
    Map<String, Object> loadSettings() throws CryptoException;

    /**
     * Check if storage files exist.
     * @return true if storage files exist, false otherwise
     */
    boolean storageExists();

    /**
     * Clear all stored data (for logout/reset).
     * @throws CryptoException If clearing fails
     */
    void clearStorage() throws CryptoException;

    /**
     * Backup all data to a specified location.
     * @param backupPath Path to save backup
     * @throws CryptoException If backup fails
     */
    void backupData(String backupPath) throws CryptoException;

    /**
     * Restore data from a backup file.
     * @param backupPath Path to restore from
     * @throws CryptoException If restore fails
     */
    void restoreData(String backupPath) throws CryptoException;
}
