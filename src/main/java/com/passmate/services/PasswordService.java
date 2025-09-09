package com.passmate.services;

import com.passmate.models.Password;
import com.passmate.models.Category;
import com.passmate.services.exceptions.CryptoException;
import com.passmate.services.impl.AESEncryptionService;
import com.passmate.services.impl.EncryptedStorageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for managing passwords with AES encryption and persistent storage.
 * Handles CRUD operations for passwords with secure storage and automatic encryption.
 */
public class PasswordService {
    private final ObservableList<Password> passwords;
    private final List<Category> categories;
    private final EncryptionService encryptionService;
    private final EncryptedStorageService storageService;
    private static PasswordService instance;
    private char[] masterKey;
    private boolean isInitialized = false;

    private PasswordService() {
        this.passwords = FXCollections.observableArrayList();
        this.categories = new ArrayList<>();
        this.encryptionService = new AESEncryptionService();
        this.storageService = new EncryptedStorageService(encryptionService);
        loadSampleDataIfNeeded();
    }

    public static PasswordService getInstance() {
        if (instance == null) {
            instance = new PasswordService();
        }
        return instance;
    }

    /**
     * Sets the master key and initializes encrypted storage.
     * @param masterKey The master key to use
     */
    public void setMasterKey(char[] masterKey) throws CryptoException {
        this.masterKey = masterKey != null ? masterKey.clone() : null;

        if (this.masterKey != null) {
            // Initialize encrypted storage
            storageService.initialize(this.masterKey);

            // Load existing data or create defaults
            loadAllData();
            isInitialized = true;

            System.out.println("PasswordService initialized with encrypted storage");
        }
    }

    /**
     * Load all data from encrypted storage
     */
    private void loadAllData() throws CryptoException {
        try {
            // Load passwords
            List<Password> loadedPasswords = storageService.loadPasswords();
            passwords.clear();
            if (loadedPasswords != null && !loadedPasswords.isEmpty()) {
                passwords.addAll(loadedPasswords);
            } else {
                // Load sample data if no passwords exist
                loadSampleDataIfNeeded();
                saveAllData(); // Save sample data to storage
            }

            // Load categories
            List<Category> loadedCategories = storageService.loadCategories();
            categories.clear();
            categories.addAll(loadedCategories);

            System.out.println("Loaded " + passwords.size() + " passwords and " + categories.size() + " categories from encrypted storage");

        } catch (CryptoException e) {
            System.err.println("Failed to load data from encrypted storage: " + e.getMessage());
            // Fall back to sample data
            loadSampleDataIfNeeded();
        }
    }

    /**
     * Save all current data to encrypted storage
     */
    private void saveAllData() throws CryptoException {
        if (!isInitialized) return;

        storageService.savePasswords(new ArrayList<>(passwords));
        storageService.saveCategories(new ArrayList<>(categories));
    }

    /**
     * Creates a new password entry with encryption and saves to storage.
     * @param name The service name
     * @param username The username
     * @param plainPassword The plain text password
     * @param website The website URL
     * @param notes Additional notes
     * @param categoryId The category ID
     * @return The created password entry
     * @throws CryptoException If encryption fails
     */
    public Password createPassword(String name, String username, String plainPassword,
                                 String website, String notes, String categoryId) throws CryptoException {
        if (masterKey == null) {
            throw new CryptoException("Master key not set");
        }

        Password password = new Password(name, username, "", website);
        password.setNotes(notes);
        password.setCategoryId(categoryId);

        // Encrypt the password
        String encryptedPassword = encryptionService.encrypt(plainPassword, masterKey);
        password.setPasswordHash(encryptedPassword);

        // Set metadata
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        password.setCreatedBy("User");
        password.setCreatedDate(currentDate);
        password.setLastUpdated(currentDate);

        passwords.add(password);

        // Save to encrypted storage
        try {
            saveAllData();
        } catch (CryptoException e) {
            // Remove from memory if save failed
            passwords.remove(password);
            throw e;
        }

        return password;
    }

    /**
     * Creates a password entry with proper encryption and category assignment.
     * @param password The password object to create
     * @throws CryptoException If encryption fails
     */
    public void createPasswordEntry(Password password) throws CryptoException {
        if (masterKey == null) {
            throw new CryptoException("Master key not set");
        }

        // Encrypt the password
        String encryptedPassword = encryptionService.encrypt(password.getPassword(), masterKey);
        password.setPasswordHash(encryptedPassword);

        // Set metadata
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        password.setCreatedBy("User");
        password.setCreatedDate(currentDate);
        password.setLastUpdated(currentDate);

        passwords.add(password);

        // Save to encrypted storage
        saveAllData();
    }

    /**
     * Updates an existing password entry and saves to storage.
     * @param password The password to update
     * @param newPlainPassword The new plain text password (if changed)
     * @throws CryptoException If encryption fails
     */
    public void updatePassword(Password password, String newPlainPassword) throws CryptoException {
        if (masterKey == null) {
            throw new CryptoException("Master key not set");
        }

        if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
            String encryptedPassword = encryptionService.encrypt(newPlainPassword, masterKey);
            password.setPasswordHash(encryptedPassword);
        }

        // Update last modified date
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        password.setLastUpdated(currentDate);

        // Save to encrypted storage
        saveAllData();
    }

    /**
     * Gets the decrypted password for display/copying.
     * @param password The password entry
     * @return The decrypted password
     * @throws CryptoException If decryption fails
     */
    public String getDecryptedPassword(Password password) throws CryptoException {
        if (masterKey == null) {
            throw new CryptoException("Master key not set");
        }

        String encryptedPassword = password.getPasswordHash();
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return password.getPassword(); // Fallback for sample data
        }

        return encryptionService.decrypt(encryptedPassword, masterKey);
    }

    /**
     * Removes a password entry and updates storage.
     * @param password The password to remove
     */
    public void deletePassword(Password password) {
        passwords.remove(password);
        try {
            saveAllData();
        } catch (CryptoException e) {
            System.err.println("Failed to save after password deletion: " + e.getMessage());
        }
    }

    /**
     * Duplicates a password entry and saves to storage.
     * @param original The original password to duplicate
     * @return The duplicated password entry
     */
    public Password duplicatePassword(Password original) {
        Password duplicate = new Password(
            original.getName() + " (Copy)",
            original.getUsername(),
            original.getPassword(),
            original.getWebsite()
        );

        duplicate.setNotes(original.getNotes());
        duplicate.setCategoryId(original.getCategoryId());
        duplicate.setPasswordHash(original.getPasswordHash());

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        duplicate.setCreatedBy("User");
        duplicate.setCreatedDate(currentDate);
        duplicate.setLastUpdated(currentDate);

        passwords.add(duplicate);

        try {
            saveAllData();
        } catch (CryptoException e) {
            System.err.println("Failed to save duplicated password: " + e.getMessage());
        }

        return duplicate;
    }

    /**
     * Creates a new category and saves to storage.
     * @param category The category to create
     */
    public void createCategory(Category category) {
        if (!categories.stream().anyMatch(c -> c.getId().equals(category.getId()))) {
            categories.add(category);
            try {
                saveAllData();
            } catch (CryptoException e) {
                System.err.println("Failed to save new category: " + e.getMessage());
            }
        }
    }

    /**
     * Renames a category and saves to storage.
     * @param categoryId The category ID to rename
     * @param newName The new category name
     */
    public void renameCategory(String categoryId, String newName) {
        categories.stream()
            .filter(c -> c.getId().equals(categoryId))
            .findFirst()
            .ifPresent(category -> {
                category.setName(newName);
                try {
                    saveAllData();
                } catch (CryptoException e) {
                    System.err.println("Failed to save renamed category: " + e.getMessage());
                }
            });
    }

    /**
     * Deletes a category and moves passwords to 'personal' category.
     * @param categoryId The category ID to delete
     */
    public void deleteCategory(String categoryId) {
        // Don't delete default categories
        if ("personal".equals(categoryId) || "work".equals(categoryId) || "games".equals(categoryId)) {
            return;
        }

        // Move passwords from deleted category to personal
        passwords.stream()
            .filter(p -> categoryId.equals(p.getCategoryId()))
            .forEach(p -> p.setCategoryId("personal"));

        // Remove category
        categories.removeIf(c -> c.getId().equals(categoryId));

        try {
            saveAllData();
        } catch (CryptoException e) {
            System.err.println("Failed to save after category deletion: " + e.getMessage());
        }
    }

    /**
     * Gets all passwords.
     * @return Observable list of passwords
     */
    public ObservableList<Password> getAllPasswords() {
        return passwords;
    }

    /**
     * Gets all categories.
     * @return List of categories
     */
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Backup all data to specified path.
     * @param backupPath Path to save backup
     * @throws CryptoException If backup fails
     */
    public void backupData(String backupPath) throws CryptoException {
        if (!isInitialized) {
            throw new CryptoException("Service not initialized");
        }
        storageService.backupData(backupPath);
    }

    /**
     * Restore data from backup and reload.
     * @param backupPath Path to restore from
     * @throws CryptoException If restore fails
     */
    public void restoreData(String backupPath) throws CryptoException {
        if (!isInitialized) {
            throw new CryptoException("Service not initialized");
        }
        storageService.restoreData(backupPath);
        loadAllData(); // Reload data after restore
    }

    /**
     * Clear all data and storage.
     * @throws CryptoException If clearing fails
     */
    public void clearAllData() throws CryptoException {
        passwords.clear();
        categories.clear();
        if (isInitialized) {
            storageService.clearStorage();
        }
    }

    /**
     * Load sample data only if no existing data
     */
    private void loadSampleDataIfNeeded() {
        if (passwords.isEmpty()) {
            // Sample passwords for demonstration
            passwords.addAll(Arrays.asList(
                new Password("Adobe Cloud", "aron.vane@email.com", "SecurePass123", "creativecloud.adobe.com"),
                new Password("Airtable", "aron.vane", "AirTable456", "airtable.com"),
                new Password("Webflow", "aronvane", "WebFlow789", "webflow.com"),
                new Password("Framer", "aron.vane@email.com", "Frame2023", "framer.com"),
                new Password("Amazon", "aron.vane@email.com", "Amazon999", "amazon.com"),
                new Password("Google", "aron.vane@gmail.com", "Google123!", "google.com"),
                new Password("Apple ID", "aron.vane@icloud.com", "Apple2023", "apple.com"),
                new Password("Superhuman", "aron.vane@email.com", "Super2023", "superhuman.com"),
                new Password("Instagram", "aronvane", "Insta2023", "instagram.com")
            ));

            // Set creation dates for sample data
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            for (Password password : passwords) {
                password.setCreatedBy("User");
                password.setCreatedDate(currentDate);
                password.setLastUpdated(currentDate);
            }
        }

        if (categories.isEmpty()) {
            categories.addAll(Arrays.asList(
                new Category("personal", "Personal", "folder-personal-icon"),
                new Category("work", "Work", "folder-work-icon"),
                new Category("games", "Games", "folder-games-icon")
            ));
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (masterKey != null) {
            Arrays.fill(masterKey, '\0');
            masterKey = null;
        }
        if (storageService != null) {
            storageService.cleanup();
        }
        isInitialized = false;
    }
}
