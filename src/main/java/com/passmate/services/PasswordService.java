package com.passmate.services;

import com.passmate.models.Password;

import java.util.List;
import java.util.UUID;

/**
 * Handles encrypted password operations using an EncryptionService and persisting via VaultService.
 */
public interface PasswordService {
    /**
     * Create an encrypted password entry and persist it.
     */
    Password create(String title, String username, String plaintextPassword, UUID categoryId, char[] masterKey);

    /**
     * Decrypt a password entry to plaintext using the master key.
     */
    String decrypt(Password entry, char[] masterKey);

    /**
     * List passwords within a category.
     */
    List<Password> listByCategory(UUID categoryId);

    /**
     * Delete a password by id.
     */
    void delete(UUID id);

    /**
     * Update fields of an existing password entry. If newPlaintextPassword is null, the stored hash is kept.
     * If newPlaintextPassword is non-null, it will be encrypted with the provided masterKey.
     */
    Password update(Password entry, String title, String username, String newPlaintextPassword, char[] masterKey);
}
