package com.naminhyeok.fantazzk.template;

public abstract class TemplateCatalogException extends RuntimeException {

    private final TemplateId templateId;

    protected TemplateCatalogException(TemplateId templateId, String message) {
        super(message);
        this.templateId = templateId;
    }

    public TemplateId getTemplateId() {
        return templateId;
    }

    public static final class NotFound extends TemplateCatalogException {

        public NotFound(TemplateId templateId) {
            super(templateId, "템플릿을 찾을 수 없습니다");
        }
    }

    public static final class Invalid extends TemplateCatalogException {

        public Invalid(TemplateId templateId) {
            super(templateId, "유효하지 않은 템플릿입니다");
        }
    }
}
