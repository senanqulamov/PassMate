package com.passmate.services.impl;

import com.passmate.services.EncryptionService;
import com.passmate.services.exceptions.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Arrays;

/**
 * AES-256-GCM encryption service implementation.
 * Provides secure encryption/decryption with authentication.
 */
public class AESEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int SALT_LENGTH = 32;

    /**
     * Encrypts plaintext using AES-256-GCM with PBKDF2 key derivation.
     * Format: [salt][iv][ciphertext+tag]
     */
    @Override
    public String encrypt(String plaintext, char[] masterKey) throws CryptoException {
        if (plaintext == null || masterKey == null) {
            throw new CryptoException("Plaintext and master key cannot be null");
        }

        try {
            // Generate random salt and IV
            byte[] salt = generateSalt();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Derive key from master password
            byte[] derivedKey = deriveKey(masterKey, salt);
            SecretKey secretKey = new SecretKeySpec(derivedKey, ALGORITHM);

            // Encrypt with AES-GCM
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Combine salt + IV + ciphertext
            byte[] result = new byte[salt.length + iv.length + ciphertext.length];
            System.arraycopy(salt, 0, result, 0, salt.length);
            System.arraycopy(iv, 0, result, salt.length, iv.length);
            System.arraycopy(ciphertext, 0, result, salt.length + iv.length, ciphertext.length);

            // Clear sensitive data
            Arrays.fill(derivedKey, (byte) 0);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new CryptoException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts ciphertext using AES-256-GCM with PBKDF2 key derivation.
     */
    @Override
    public String decrypt(String ciphertext, char[] masterKey) throws CryptoException {
        if (ciphertext == null || masterKey == null) {
            throw new CryptoException("Ciphertext and master key cannot be null");
        }

        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            if (combined.length < SALT_LENGTH + GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new CryptoException("Invalid ciphertext format");
            }

            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - SALT_LENGTH - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            // Derive key from master password
            byte[] derivedKey = deriveKey(masterKey, salt);
            SecretKey secretKey = new SecretKeySpec(derivedKey, ALGORITHM);

            // Decrypt with AES-GCM
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] decrypted = cipher.doFinal(encryptedData);

            // Clear sensitive data
            Arrays.fill(derivedKey, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            Arrays.fill(encryptedData, (byte) 0);

            return new String(decrypted, "UTF-8");

        } catch (Exception e) {
            throw new CryptoException("Decryption failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    @Override
    public byte[] deriveKey(char[] masterPassword, byte[] salt) throws CryptoException {
        try {
            KeySpec spec = new PBEKeySpec(masterPassword, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new CryptoException("Key derivation failed: " + e.getMessage(), e);
        }
    }
}
