package com.passmate.services.repo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * File-based repository for storing a user's PIN hash metadata under ~/.passmate/pin.json
 */
public class FilePinRepository {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path file;

    public FilePinRepository() {
        String userHome = System.getProperty("user.home");
        Path dir = Path.of(userHome, ".passmate");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        this.file = dir.resolve("pin.json");
    }

    public FilePinRepository(Path file) {
        this.file = file;
        try { Files.createDirectories(file.getParent()); } catch (IOException ignored) {}
    }

    public boolean exists() {
        return Files.exists(file);
    }

    public PinRecord load() {
        if (!exists()) return null;
        try {
            return mapper.readValue(file.toFile(), PinRecord.class);
        } catch (IOException e) {
            return null;
        }
    }

    public void save(PinRecord record) {
        try {
            Path tmp = Files.createTempFile(file.getParent(), "pin", ".json.tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), record);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save PIN", e);
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {}
    }

    public static class PinRecord {
        public String version = "v1";
        public String algo = "PBKDF2WithHmacSHA256";
        public int iterations;
        public String saltB64;
        public String hashB64;
    }
}

