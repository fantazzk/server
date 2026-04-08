package com.naminhyeok.fantazzk;

public class CoreException extends RuntimeException {
    private final ErrorDescriptor error;
    private final Object data;

    public static CoreException of(ErrorDescriptor error) {
        return new CoreException(error);
    }

    public static CoreException of(ErrorDescriptor error, Object data) {
        return new CoreException(error, data);
    }

    public CoreException(ErrorDescriptor error) {
        this(error, null);
    }

    public CoreException(ErrorDescriptor error, Object data) {
        super(error.getMessage());
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
