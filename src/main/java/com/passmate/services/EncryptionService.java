package com.passmate.services;

import com.passmate.services.exceptions.CryptoException;

/**
 * Symmetric encryption service. Default: AES-256-GCM with PBKDF2 key derivation.
 */
public interface EncryptionService {
    /**
     * Encrypt plaintext with a user-provided master key (e.g., passphrase) and return a base64 string payload.
     */
    String encrypt(String plaintext, char[] masterKey) throws CryptoException;

    /**
     * Decrypt a base64 string payload with the user-provided master key.
     */
    String decrypt(String ciphertextBase64, char[] masterKey) throws CryptoException;
}

