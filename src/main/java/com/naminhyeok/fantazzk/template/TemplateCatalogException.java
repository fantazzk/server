package com.naminhyeok.fantazzk.template;

public sealed class TemplateCatalogException extends RuntimeException permits TemplateCatalogException.NotFound {
    private TemplateCatalogException(String message) {
        super(message);
    }

    public static final class NotFound extends TemplateCatalogException {
        public NotFound(TemplateId templateId) {
            super("템플릿을 찾을 수 없습니다: " + templateId);
        }
    }
}
