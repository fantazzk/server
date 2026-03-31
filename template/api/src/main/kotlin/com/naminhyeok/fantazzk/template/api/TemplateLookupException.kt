package com.naminhyeok.fantazzk.template.api

sealed class TemplateLookupException(message: String) : RuntimeException(message) {
    class NotFound(
        val templateId: Long,
    ) : TemplateLookupException("템플릿을 찾을 수 없습니다")

    class Invalid(
        val templateId: Long,
    ) : TemplateLookupException("유효하지 않은 템플릿입니다")
}
