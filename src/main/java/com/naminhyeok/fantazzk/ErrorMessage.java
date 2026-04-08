package com.naminhyeok.fantazzk;

public record ErrorMessage(String code, String message, Object data) {
    public ErrorMessage(ErrorDescriptor descriptor) {
        this(descriptor, null);
    }

    public ErrorMessage(ErrorDescriptor descriptor, Object data) {
        this(descriptor.getCode(), descriptor.getMessage(), data);
    }
}
