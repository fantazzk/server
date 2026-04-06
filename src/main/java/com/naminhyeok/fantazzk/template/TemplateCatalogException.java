package com.naminhyeok.fantazzk.template;

public abstract class TemplateCatalogException extends RuntimeException {
    private TemplateCatalogException(String message) {
        super(message);
    }

    public static final class NotFound extends TemplateCatalogException {
        private final TemplateId templateId;

        public NotFound(TemplateId templateId) {
            super("템플릿을 찾을 수 없습니다");
            this.templateId = templateId;
        }

        public TemplateId getTemplateId() {
            return templateId;
        }
    }

    public static final class Invalid extends TemplateCatalogException {
        private final TemplateId templateId;

        public Invalid(TemplateId templateId) {
            super("유효하지 않은 템플릿입니다");
            this.templateId = templateId;
        }

        public TemplateId getTemplateId() {
            return templateId;
        }
    }
}
