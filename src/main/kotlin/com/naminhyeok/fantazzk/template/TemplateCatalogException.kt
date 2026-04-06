package com.naminhyeok.fantazzk.template

sealed class TemplateCatalogException(
    val templateId: TemplateId,
    message: String,
) : RuntimeException(message) {
    class NotFound(
        templateId: TemplateId,
    ) : TemplateCatalogException(templateId, "템플릿을 찾을 수 없습니다")

    class Invalid(
        templateId: TemplateId,
    ) : TemplateCatalogException(templateId, "유효하지 않은 템플릿입니다")
}
