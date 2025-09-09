package com.passmate.services;

import com.passmate.services.exceptions.CryptoException;

/**
 * Service interface for encryption and decryption operations.
 * Provides secure AES-256 encryption for password storage.
 */
public interface EncryptionService {

    /**
     * Encrypts the given plaintext using AES-256 encryption.
     * @param plaintext The text to encrypt
     * @param masterKey The master key for encryption
     * @return The encrypted text as a Base64 encoded string
     * @throws CryptoException If encryption fails
     */
    String encrypt(String plaintext, char[] masterKey) throws CryptoException;

    /**
     * Decrypts the given ciphertext using AES-256 encryption.
     * @param ciphertext The encrypted text as a Base64 encoded string
     * @param masterKey The master key for decryption
     * @return The decrypted plaintext
     * @throws CryptoException If decryption fails
     */
    String decrypt(String ciphertext, char[] masterKey) throws CryptoException;

    /**
     * Generates a random salt for key derivation.
     * @return A byte array containing the random salt
     */
    byte[] generateSalt();

    /**
     * Derives a key from the master password and salt using PBKDF2.
     * @param masterPassword The master password
     * @param salt The salt for key derivation
     * @return The derived key
     * @throws CryptoException If key derivation fails
     */
    byte[] deriveKey(char[] masterPassword, byte[] salt) throws CryptoException;
}
