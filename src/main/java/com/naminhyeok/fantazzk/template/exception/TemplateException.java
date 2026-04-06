package com.naminhyeok.fantazzk.template.exception;

public abstract class TemplateException extends RuntimeException {
    private final String errorCode;

    protected TemplateException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static final class TemplateNotFoundException extends TemplateException {
        public TemplateNotFoundException() {
            super("TEMPLATE_NOT_FOUND", "템플릿을 찾을 수 없습니다");
        }
    }

    public static final class TemplateInvalidException extends TemplateException {
        public TemplateInvalidException() {
            super("TEMPLATE_INVALID", "유효하지 않은 템플릿입니다");
        }
    }
}
