package com.passmate.services.impl;

import com.passmate.models.Password;
import com.passmate.services.EncryptionService;
import com.passmate.services.PasswordService;
import com.passmate.services.VaultService;

import java.util.List;
import java.util.UUID;

/**
 * Default PasswordService that encrypts with EncryptionService and persists via VaultService.
 */
public class PasswordServiceImpl implements PasswordService {
    private final EncryptionService encryptionService;
    private final VaultService vaultService;

    public PasswordServiceImpl(EncryptionService encryptionService, VaultService vaultService) {
        this.encryptionService = encryptionService;
        this.vaultService = vaultService;
    }

    @Override
    public Password create(String title, String username, String plaintextPassword, UUID categoryId, char[] masterKey) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (plaintextPassword == null) throw new IllegalArgumentException("password required");
        if (categoryId == null) throw new IllegalArgumentException("categoryId required");
        String cipher = encryptionService.encrypt(plaintextPassword, masterKey);
        Password entry = new Password(title.trim(), username == null ? "" : username.trim(), cipher, categoryId);
        vaultService.addPassword(entry);
        return entry;
    }

    @Override
    public String decrypt(Password entry, char[] masterKey) {
        if (entry == null) throw new IllegalArgumentException("entry is null");
        return encryptionService.decrypt(entry.getPasswordHash(), masterKey);
    }

    @Override
    public List<Password> listByCategory(UUID categoryId) {
        return vaultService.getPasswordsByCategory(categoryId);
    }

    @Override
    public void delete(UUID id) {
        vaultService.deletePassword(id);
    }

    @Override
    public Password update(Password entry, String title, String username, String newPlaintextPassword, char[] masterKey) {
        if (entry == null) throw new IllegalArgumentException("entry is null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        entry.setTitle(title.trim());
        entry.setUsername(username == null ? "" : username.trim());
        if (newPlaintextPassword != null) {
            String cipher = encryptionService.encrypt(newPlaintextPassword, masterKey);
            entry.setPasswordHash(cipher);
        }
        vaultService.updatePassword(entry);
        return entry;
    }
}
