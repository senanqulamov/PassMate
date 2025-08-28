package com.passmate.services;

/**
 * App PIN authentication service.
 * Implementations must not persist raw PINs; store only secure hashes or encrypted forms.
 */
public interface PinService {
    /**
     * @return true if a PIN has been set for the current user
     */
    boolean hasPin();

    /**
     * Validate the provided PIN.
     * @param pin the PIN characters; caller will clear this array after use
     * @return true if authentication succeeds, false otherwise
     */
    boolean validatePin(char[] pin);

    /**
     * Set or replace the PIN for the current user.
     * @param pin plaintext PIN characters; implementation must hash/encrypt securely
     */
    void setPin(char[] pin);

    /**
     * Clear/remove the stored PIN for the current user (e.g., sign out or reset).
     */
    void clearPin();
}
