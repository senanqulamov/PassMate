package com.passmate.services.impl;

import com.passmate.models.Category;
import com.passmate.models.Vault;
import com.passmate.services.VaultService;
import com.passmate.services.exceptions.CategoryNotFoundException;
import com.passmate.services.exceptions.DuplicateCategoryException;
import com.passmate.services.repo.VaultRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In-memory implementation of VaultService with optional file persistence.
 */
public class VaultServiceImpl implements VaultService {
    private final VaultRepository repository;
    private final Vault vault;

    public VaultServiceImpl() {
        this.repository = null;
        this.vault = new Vault();
    }

    public VaultServiceImpl(Vault vault) {
        this.repository = null;
        this.vault = vault;
    }

    public VaultServiceImpl(VaultRepository repository) {
        this.repository = repository;
        this.vault = repository != null ? repository.load() : new Vault();
    }

    private void persist() {
        if (repository != null) {
            repository.save(vault);
        }
    }

    @Override
    public List<Category> getCategories() {
        return vault.getCategories().stream()
                .sorted(Comparator.comparing(c -> c.getName().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    @Override
    public Category createCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        String trimmed = name.trim();
        if (existsByNameIgnoreCase(trimmed)) {
            throw new DuplicateCategoryException(trimmed);
        }
        Category category = new Category(trimmed);
        vault.addCategory(category);
        persist();
        return category;
    }

    @Override
    public Category renameCategory(UUID id, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        String target = newName.trim();
        Category cat = findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
        // Allow same id same name (case-insensitive) to pass-through
        Optional<Category> duplicate = getCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(target))
                .findFirst();
        if (duplicate.isPresent() && !duplicate.get().getId().equals(cat.getId())) {
            throw new DuplicateCategoryException(target);
        }
        cat.setName(target);
        persist();
        return cat;
    }

    @Override
    public void deleteCategory(UUID id) {
        boolean removed = vault.removeCategory(id);
        if (!removed) {
            throw new CategoryNotFoundException(id);
        }
        persist();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        if (name == null) return false;
        String n = name.trim().toLowerCase(Locale.ROOT);
        return vault.getCategories().stream()
                .anyMatch(c -> c.getName().toLowerCase(Locale.ROOT).equals(n));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return vault.getCategories().stream().filter(c -> c.getId().equals(id)).findFirst();
    }
}
