package com.passmate.services.impl;

import com.passmate.services.EncryptionService;
import com.passmate.services.exceptions.CryptoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESEncryptionServiceTest {

    @Test
    void roundtrip_encrypt_decrypt_ok() {
        EncryptionService svc = new AESEncryptionService();
        String ct = svc.encrypt("secret-🙂", "master-key".toCharArray());
        assertNotNull(ct);
        assertTrue(ct.startsWith("v1:"));
        String pt = svc.decrypt(ct, "master-key".toCharArray());
        assertEquals("secret-🙂", pt);
    }

    @Test
    void decrypt_with_wrong_key_fails() {
        EncryptionService svc = new AESEncryptionService();
        String ct = svc.encrypt("secret", "right".toCharArray());
        assertThrows(CryptoException.class, () -> svc.decrypt(ct, "wrong".toCharArray()));
    }
}

