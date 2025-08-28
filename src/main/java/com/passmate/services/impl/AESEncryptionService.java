package com.passmate.services.impl;

import com.passmate.services.EncryptionService;
import com.passmate.services.exceptions.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption with PBKDF2-HMAC-SHA256 key derivation.
 * Payload format (Base64 for each part):
 * v1:<salt>:<iv>:<ciphertext>
 */
public class AESEncryptionService implements EncryptionService {
    private static final String VERSION = "v1";
    private static final int SALT_LEN = 16; // 128-bit
    private static final int IV_LEN = 12; // 96-bit for GCM
    private static final int KEY_LEN = 256; // bits
    private static final int ITERATIONS = 100_000;
    private static final int TAG_BITS = 128;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String encrypt(String plaintext, char[] masterKey) throws CryptoException {
        if (plaintext == null) throw new CryptoException("plaintext is null");
        if (masterKey == null || masterKey.length == 0) throw new CryptoException("masterKey is empty");
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(salt);
        random.nextBytes(iv);
        try {
            SecretKeySpec key = deriveKey(masterKey, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return VERSION + ":" + b64(salt) + ":" + b64(iv) + ":" + b64(ct);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertextBase64, char[] masterKey) throws CryptoException {
        if (ciphertextBase64 == null || ciphertextBase64.isBlank()) throw new CryptoException("ciphertext is empty");
        if (masterKey == null || masterKey.length == 0) throw new CryptoException("masterKey is empty");
        try {
            String[] parts = ciphertextBase64.split(":" , 4);
            if (parts.length != 4 || !VERSION.equals(parts[0])) throw new CryptoException("unsupported payload");
            byte[] salt = b64d(parts[1]);
            byte[] iv = b64d(parts[2]);
            byte[] ct = b64d(parts[3]);
            SecretKeySpec key = deriveKey(masterKey, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("decryption failed", e);
        }
    }

    private static SecretKeySpec deriveKey(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LEN);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String b64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }
    private static byte[] b64d(String s) {
        return Base64.getDecoder().decode(s);
    }
}

