package com.naminhyeok.fantazzk.template

sealed class TemplateCatalogException(message: String) : RuntimeException(message) {
    data class NotFound(
        val templateId: TemplateId,
    ) : TemplateCatalogException("템플릿을 찾을 수 없습니다")

    data class Invalid(
        val templateId: TemplateId,
    ) : TemplateCatalogException("유효하지 않은 템플릿입니다")
}
