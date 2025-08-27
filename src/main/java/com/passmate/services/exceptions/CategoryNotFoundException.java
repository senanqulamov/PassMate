package com.passmate.services.exceptions;

import java.util.UUID;

/**
 * Thrown when a category with the given id cannot be found.
 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID id) {
        super("Category not found: " + id);
    }
}

