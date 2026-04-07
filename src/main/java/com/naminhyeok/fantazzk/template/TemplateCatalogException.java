package com.naminhyeok.fantazzk.template;

import java.util.UUID;

public sealed class TemplateCatalogException extends RuntimeException permits TemplateCatalogException.NotFound {
    private TemplateCatalogException(String message) {
        super(message);
    }

    public static final class NotFound extends TemplateCatalogException {
        public NotFound(UUID templateId) {
            super("템플릿을 찾을 수 없습니다: " + templateId);
        }
    }
}
