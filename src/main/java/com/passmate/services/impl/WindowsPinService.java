package com.passmate.services.impl;

import com.passmate.services.PinService;

/**
 * Deprecated: Windows OS auth is no longer used. This stub satisfies the interface.
 */
@Deprecated
public class WindowsPinService implements PinService {
    @Override
    public boolean hasPin() {
        return false;
    }

    @Override
    public boolean validatePin(char[] pin) {
        return false;
    }

    @Override
    public void setPin(char[] pin) {
        throw new UnsupportedOperationException("WindowsPinService is deprecated");
    }

    @Override
    public void clearPin() {
        // no-op
    }
}
