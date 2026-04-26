package com.naminhyeok.fantazzk;

import lombok.Getter;

@Getter
public abstract class InvalidDomainStateException extends RuntimeException {
    private final ErrorDescriptor error;

    protected InvalidDomainStateException(String message, ErrorDescriptor error) {
        super(message);
        this.error = error;
    }
}
