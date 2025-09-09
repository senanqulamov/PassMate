package com.passmate.services.impl;

import com.passmate.models.Password;
import com.passmate.models.Category;
import com.passmate.models.Vault;
import com.passmate.models.StorageData;
import com.passmate.services.StorageService;
import com.passmate.services.EncryptionService;
import com.passmate.services.exceptions.CryptoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Implementation of StorageService that encrypts all data using AES and stores it in JSON files.
 * All passwords, categories, vaults, and settings are encrypted before storage.
 */
public class EncryptedStorageService implements StorageService {

    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".passmate";
    private static final String PASSWORDS_FILE = "passwords.enc";
    private static final String CATEGORIES_FILE = "categories.enc";
    private static final String VAULT_FILE = "vault.enc";
    private static final String SETTINGS_FILE = "settings.enc";
    private static final String BACKUP_EXTENSION = ".backup";

    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private char[] masterKey;
    private Path storagePath;

    public EncryptedStorageService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public void initialize(char[] masterKey) throws CryptoException {
        if (masterKey == null || masterKey.length == 0) {
            throw new CryptoException("Master key cannot be null or empty");
        }

        this.masterKey = Arrays.copyOf(masterKey, masterKey.length);
        this.storagePath = Paths.get(STORAGE_DIR);

        try {
            // Create storage directory if it doesn't exist
            Files.createDirectories(storagePath);

            // Set directory permissions (owner only)
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                Files.setPosixFilePermissions(storagePath,
                    Set.of(java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                           java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                           java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE));
            }

            System.out.println("Encrypted storage initialized at: " + storagePath.toString());

        } catch (IOException e) {
            throw new CryptoException("Failed to initialize storage directory: " + e.getMessage(), e);
        }
    }

    @Override
    public void savePasswords(List<Password> passwords) throws CryptoException {
        validateInitialized();
        saveEncryptedData(passwords, PASSWORDS_FILE);
        System.out.println("Saved " + passwords.size() + " passwords to encrypted storage");
    }

    @Override
    public List<Password> loadPasswords() throws CryptoException {
        validateInitialized();
        List<Password> passwords = loadEncryptedData(PASSWORDS_FILE,
            objectMapper.getTypeFactory().constructCollectionType(List.class, Password.class));

        if (passwords == null) {
            passwords = new ArrayList<>();
        }

        System.out.println("Loaded " + passwords.size() + " passwords from encrypted storage");
        return passwords;
    }

    @Override
    public void saveCategories(List<Category> categories) throws CryptoException {
        validateInitialized();
        saveEncryptedData(categories, CATEGORIES_FILE);
        System.out.println("Saved " + categories.size() + " categories to encrypted storage");
    }

    @Override
    public List<Category> loadCategories() throws CryptoException {
        validateInitialized();
        List<Category> categories = loadEncryptedData(CATEGORIES_FILE,
            objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));

        if (categories == null) {
            categories = new ArrayList<>();
            // Add default categories
            categories.add(new Category("personal", "Personal", "folder-personal-icon"));
            categories.add(new Category("work", "Work", "folder-work-icon"));
            categories.add(new Category("games", "Games", "folder-games-icon"));
        }

        System.out.println("Loaded " + categories.size() + " categories from encrypted storage");
        return categories;
    }

    @Override
    public void saveVault(Vault vault) throws CryptoException {
        validateInitialized();
        saveEncryptedData(vault, VAULT_FILE);
        System.out.println("Saved vault configuration to encrypted storage");
    }

    @Override
    public Vault loadVault() throws CryptoException {
        validateInitialized();
        Vault vault = loadEncryptedData(VAULT_FILE, Vault.class);

        if (vault == null) {
            // Create default vault
            vault = new Vault("My Vault", "Default User");
        }

        System.out.println("Loaded vault configuration from encrypted storage");
        return vault;
    }

    @Override
    public void saveSettings(Map<String, Object> settings) throws CryptoException {
        validateInitialized();
        saveEncryptedData(settings, SETTINGS_FILE);
        System.out.println("Saved application settings to encrypted storage");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadSettings() throws CryptoException {
        validateInitialized();
        Map<String, Object> settings = loadEncryptedData(SETTINGS_FILE, Map.class);

        if (settings == null) {
            settings = new HashMap<>();
            // Add default settings
            settings.put("theme", "light");
            settings.put("autoLock", true);
            settings.put("autoLockTimeout", 300); // 5 minutes
            settings.put("clipboardTimeout", 30); // 30 seconds
            settings.put("showToasts", true);
        }

        System.out.println("Loaded application settings from encrypted storage");
        return settings;
    }

    @Override
    public boolean storageExists() {
        if (storagePath == null) return false;

        return Files.exists(storagePath.resolve(PASSWORDS_FILE)) ||
               Files.exists(storagePath.resolve(CATEGORIES_FILE)) ||
               Files.exists(storagePath.resolve(VAULT_FILE)) ||
               Files.exists(storagePath.resolve(SETTINGS_FILE));
    }

    @Override
    public void clearStorage() throws CryptoException {
        validateInitialized();

        try {
            deleteFileIfExists(PASSWORDS_FILE);
            deleteFileIfExists(CATEGORIES_FILE);
            deleteFileIfExists(VAULT_FILE);
            deleteFileIfExists(SETTINGS_FILE);

            System.out.println("Cleared all encrypted storage files");

        } catch (IOException e) {
            throw new CryptoException("Failed to clear storage: " + e.getMessage(), e);
        }
    }

    @Override
    public void backupData(String backupPath) throws CryptoException {
        validateInitialized();

        try {
            Path backup = Paths.get(backupPath);
            Files.createDirectories(backup.getParent());

            // Create a complete backup with all data
            StorageData allData = new StorageData();
            allData.setPasswords(loadPasswords());
            allData.setCategories(loadCategories());
            allData.setVault(loadVault());
            allData.setSettings(loadSettings());

            // Encrypt and save backup
            String json = objectMapper.writeValueAsString(allData);
            String encryptedData = encryptionService.encrypt(json, masterKey);

            Files.write(backup, encryptedData.getBytes());

            System.out.println("Created encrypted backup at: " + backupPath);

        } catch (Exception e) {
            throw new CryptoException("Failed to create backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreData(String backupPath) throws CryptoException {
        validateInitialized();

        try {
            Path backup = Paths.get(backupPath);
            if (!Files.exists(backup)) {
                throw new CryptoException("Backup file not found: " + backupPath);
            }

            // Read and decrypt backup
            String encryptedData = new String(Files.readAllBytes(backup));
            String json = encryptionService.decrypt(encryptedData, masterKey);

            StorageData allData = objectMapper.readValue(json, StorageData.class);

            // Restore all data
            if (allData.getPasswords() != null) {
                savePasswords(allData.getPasswords());
            }
            if (allData.getCategories() != null) {
                saveCategories(allData.getCategories());
            }
            if (allData.getVault() != null) {
                saveVault(allData.getVault());
            }
            if (allData.getSettings() != null) {
                saveSettings(allData.getSettings());
            }

            System.out.println("Restored data from backup: " + backupPath);

        } catch (Exception e) {
            throw new CryptoException("Failed to restore from backup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void validateInitialized() throws CryptoException {
        if (masterKey == null || storagePath == null) {
            throw new CryptoException("Storage service not initialized. Call initialize() first.");
        }
    }

    private <T> void saveEncryptedData(T data, String filename) throws CryptoException {
        try {
            // Convert to JSON
            String json = objectMapper.writeValueAsString(data);

            // Encrypt JSON
            String encryptedData = encryptionService.encrypt(json, masterKey);

            // Write to file
            Path filePath = storagePath.resolve(filename);
            Files.write(filePath, encryptedData.getBytes());

            // Set file permissions (owner only)
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                Files.setPosixFilePermissions(filePath,
                    Set.of(java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                           java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
            }

        } catch (Exception e) {
            throw new CryptoException("Failed to save encrypted data to " + filename + ": " + e.getMessage(), e);
        }
    }

    private <T> T loadEncryptedData(String filename, Class<T> type) throws CryptoException {
        return loadEncryptedData(filename, objectMapper.getTypeFactory().constructType(type));
    }

    private <T> T loadEncryptedData(String filename, com.fasterxml.jackson.databind.JavaType type) throws CryptoException {
        try {
            Path filePath = storagePath.resolve(filename);

            if (!Files.exists(filePath)) {
                return null; // File doesn't exist, return null
            }

            // Read encrypted data
            String encryptedData = new String(Files.readAllBytes(filePath));

            // Decrypt data
            String json = encryptionService.decrypt(encryptedData, masterKey);

            // Parse JSON
            return objectMapper.readValue(json, type);

        } catch (Exception e) {
            throw new CryptoException("Failed to load encrypted data from " + filename + ": " + e.getMessage(), e);
        }
    }

    private void deleteFileIfExists(String filename) throws IOException {
        Path filePath = storagePath.resolve(filename);
        Files.deleteIfExists(filePath);
    }

    // Cleanup method
    public void cleanup() {
        if (masterKey != null) {
            Arrays.fill(masterKey, '\0');
            masterKey = null;
        }
    }
}
