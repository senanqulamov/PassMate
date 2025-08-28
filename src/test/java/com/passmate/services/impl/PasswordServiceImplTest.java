package com.passmate.services.impl;

import com.passmate.models.Category;
import com.passmate.models.Password;
import com.passmate.services.EncryptionService;
import com.passmate.services.PasswordService;
import com.passmate.services.VaultService;
import com.passmate.services.exceptions.CryptoException;
import com.passmate.services.repo.FileVaultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceImplTest {

    @TempDir
    Path temp;

    @Test
    void createDecryptPersistFlow() {
        FileVaultRepository repo = new FileVaultRepository(temp.resolve("vault.json"));
        VaultService vaultService = new VaultServiceImpl(repo);
        EncryptionService enc = new AESEncryptionService();
        PasswordService svc = new PasswordServiceImpl(enc, vaultService);

        Category cat = vaultService.createCategory("Web");
        char[] key = "correct horse battery staple".toCharArray();

        Password p = svc.create("Github", "octocat", "p@ssW0rd", cat.getId(), key);
        assertNotNull(p.getId());
        assertTrue(p.getPasswordHash().startsWith("v1:"));
        List<Password> first = svc.listByCategory(cat.getId());
        assertEquals(1, first.size());
        assertEquals("Github", first.get(0).getTitle());

        // New service instance loads from file
        VaultService vaultService2 = new VaultServiceImpl(repo);
        PasswordService svc2 = new PasswordServiceImpl(enc, vaultService2);
        List<Password> loaded = svc2.listByCategory(cat.getId());
        assertEquals(1, loaded.size());
        assertEquals("p@ssW0rd", svc2.decrypt(loaded.get(0), key));

        // Wrong key should fail
        assertThrows(CryptoException.class, () -> svc2.decrypt(loaded.get(0), "wrong".toCharArray()));

        // Delete and persist
        svc2.delete(loaded.get(0).getId());
        VaultService vaultService3 = new VaultServiceImpl(repo);
        PasswordService svc3 = new PasswordServiceImpl(enc, vaultService3);
        assertTrue(svc3.listByCategory(cat.getId()).isEmpty());
    }
}

