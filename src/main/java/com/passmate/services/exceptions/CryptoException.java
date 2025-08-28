package com.passmate.services.exceptions;

/**
 * Wraps cryptographic failures to keep service signatures clean.
 */
public class CryptoException extends RuntimeException {
    public CryptoException(String message, Throwable cause) { super(message, cause); }
    public CryptoException(String message) { super(message); }
}

