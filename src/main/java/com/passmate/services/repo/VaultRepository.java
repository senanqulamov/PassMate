package com.passmate.services.repo;

import com.passmate.models.Vault;

/**
 * Persistence contract for Vault. P1 stores only categories.
 */
public interface VaultRepository {
    /** Load vault from storage. Never returns null. */
    Vault load();

    /** Persist the vault atomically if possible. */
    void save(Vault vault);
}

