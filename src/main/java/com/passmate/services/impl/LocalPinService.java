package com.passmate.services.impl;

import com.passmate.services.PinService;
import com.passmate.services.repo.FilePinRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Local PIN service storing a salted PBKDF2-HMAC-SHA256 hash under ~/.passmate/pin.json
 */
public class LocalPinService implements PinService {
    private static final int SALT_LEN = 16; // 128-bit salt
    private static final int KEY_LEN_BITS = 256;
    private static final int ITERATIONS = 200_000;

    private final SecureRandom rnd = new SecureRandom();
    private final FilePinRepository repo;

    public LocalPinService() {
        this(new FilePinRepository());
    }

    public LocalPinService(FilePinRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    @Override
    public boolean hasPin() {
        return repo.exists();
    }

    @Override
    public boolean validatePin(char[] pin) {
        if (pin == null || pin.length == 0) return false;
        FilePinRepository.PinRecord rec = repo.load();
        if (rec == null || rec.saltB64 == null || rec.hashB64 == null || rec.iterations <= 0) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(rec.saltB64);
            byte[] expected = Base64.getDecoder().decode(rec.hashB64);
            byte[] actual = derive(pin, salt, rec.iterations, KEY_LEN_BITS);
            return slowEquals(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void setPin(char[] pin) {
        if (pin == null || pin.length < 4) throw new IllegalArgumentException("PIN must be at least 4 characters");
        byte[] salt = new byte[SALT_LEN];
        rnd.nextBytes(salt);
        byte[] hash = derive(pin, salt, ITERATIONS, KEY_LEN_BITS);
        FilePinRepository.PinRecord rec = new FilePinRepository.PinRecord();
        rec.iterations = ITERATIONS;
        rec.saltB64 = Base64.getEncoder().encodeToString(salt);
        rec.hashB64 = Base64.getEncoder().encodeToString(hash);
        repo.save(rec);
    }

    @Override
    public void clearPin() {
        repo.clear();
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations, int keyLenBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("KDF failed", e);
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        int len = Math.max(a.length, b.length);
        int res = 0;
        for (int i = 0; i < len; i++) {
            byte x = i < a.length ? a[i] : 0;
            byte y = i < b.length ? b[i] : 0;
            res |= (x ^ y);
        }
        return res == 0 && a.length == b.length;
    }
}

