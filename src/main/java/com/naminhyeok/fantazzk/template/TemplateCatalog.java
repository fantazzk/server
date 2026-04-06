package com.naminhyeok.fantazzk.template;

public interface TemplateCatalog {
    TemplateBlueprint getTemplateBlueprint(TemplateId templateId);

    default TemplateBlueprint get(TemplateId templateId) {
        return getTemplateBlueprint(templateId);
    }
}
