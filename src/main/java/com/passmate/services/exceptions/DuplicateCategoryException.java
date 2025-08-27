package com.passmate.services.exceptions;

/**
 * Thrown when trying to create a category with a name that already exists (case-insensitive).
 */
public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String name) {
        super("Category already exists: " + name);
    }
}

