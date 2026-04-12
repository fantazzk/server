package com.naminhyeok.fantazzk;

public abstract class InvalidDomainStateException extends RuntimeException {
    private final ErrorDescriptor error;

    protected InvalidDomainStateException(String message, ErrorDescriptor error) {
        super(message);
        this.error = error;
    }

    public ErrorDescriptor getError() {
        return error;
    }
}
