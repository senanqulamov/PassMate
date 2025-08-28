package com.passmate.services.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passmate.models.Category;
import com.passmate.models.Password;
import com.passmate.models.Vault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * File-based JSON repository for Vault categories and passwords.
 */
public class FileVaultRepository implements VaultRepository {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path file;

    /** Use default path under user home: ~/.passmate/vault.json */
    public FileVaultRepository() {
        String userHome = System.getProperty("user.home");
        Path dir = Path.of(userHome, ".passmate");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        this.file = dir.resolve("vault.json");
    }

    /**
     * Explicit file path, useful for tests.
     */
    public FileVaultRepository(Path file) {
        this.file = file;
        try { Files.createDirectories(file.getParent()); } catch (IOException ignored) {}
    }

    @Override
    public Vault load() {
        if (!Files.exists(file)) {
            return new Vault();
        }
        try {
            VaultFileData data = mapper.readValue(file.toFile(), VaultFileData.class);
            Vault vault = new Vault();
            if (data != null) {
                if (data.categories != null) {
                    for (VaultCategory c : data.categories) {
                        UUID id = c.id != null ? UUID.fromString(c.id) : UUID.randomUUID();
                        String name = c.name != null ? c.name : "Unnamed";
                        vault.addCategory(new Category(id, name));
                    }
                }
                if (data.passwords != null) {
                    for (VaultPassword p : data.passwords) {
                        if (p.id == null || p.title == null || p.passwordHash == null || p.categoryId == null) continue;
                        UUID id = UUID.fromString(p.id);
                        UUID catId = UUID.fromString(p.categoryId);
                        String title = p.title;
                        String username = p.username == null ? "" : p.username;
                        String hash = p.passwordHash;
                        vault.addPassword(new Password(id, title, username, hash, catId));
                    }
                }
            }
            return vault;
        } catch (IOException e) {
            return new Vault();
        }
    }

    @Override
    public void save(Vault vault) {
        VaultFileData data = new VaultFileData();
        data.categories = new ArrayList<>();
        for (Category c : vault.getCategories()) {
            VaultCategory vc = new VaultCategory();
            vc.id = c.getId().toString();
            vc.name = c.getName();
            data.categories.add(vc);
        }
        data.passwords = new ArrayList<>();
        for (Password p : vault.getPasswords()) {
            VaultPassword vp = new VaultPassword();
            vp.id = p.getId().toString();
            vp.title = p.getTitle();
            vp.username = p.getUsername();
            vp.passwordHash = p.getPasswordHash();
            vp.categoryId = p.getCategoryId().toString();
            data.passwords.add(vp);
        }
        try {
            Path tmp = Files.createTempFile(file.getParent(), "vault", ".json.tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), data);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save vault", e);
        }
    }

    // DTOs for serialization
    static class VaultFileData {
        public List<VaultCategory> categories;
        public List<VaultPassword> passwords;
    }

    static class VaultCategory {
        public String id;
        public String name;
    }

    static class VaultPassword {
        public String id;
        public String title;
        public String username;
        public String passwordHash;
        public String categoryId;
    }
}
