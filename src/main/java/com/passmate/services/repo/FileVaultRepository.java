package com.passmate.services.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passmate.models.Category;
import com.passmate.models.Vault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * File-based JSON repository for Vault categories.
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
            if (data != null && data.categories != null) {
                for (VaultCategory c : data.categories) {
                    UUID id = c.id != null ? UUID.fromString(c.id) : UUID.randomUUID();
                    String name = c.name != null ? c.name : "Unnamed";
                    vault.addCategory(new Category(id, name));
                }
            }
            return vault;
        } catch (IOException e) {
            // If file corrupted, start fresh but keep file for troubleshooting
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
    }

    static class VaultCategory {
        public String id;
        public String name;
    }
}

