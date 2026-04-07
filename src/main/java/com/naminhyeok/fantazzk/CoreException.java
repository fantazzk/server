package com.naminhyeok.fantazzk;

public class CoreException extends RuntimeException {
    private final ErrorDescriptor error;
    private final Object data;

    public CoreException(ErrorDescriptor error) {
        this(error, null);
    }

    public CoreException(ErrorDescriptor error, Object data) {
        super(error.message());
        this.error = error;
        this.data = data;
    }

    public ErrorDescriptor getError() {
        return error;
    }

    public Object getData() {
        return data;
    }
}
