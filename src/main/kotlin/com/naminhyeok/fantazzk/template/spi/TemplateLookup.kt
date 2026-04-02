package com.naminhyeok.fantazzk.template.spi

interface TemplateLookup {
    fun getTemplate(templateId: Long): TemplateSnapshot
}

data class TemplateSnapshot(
    val mode: TemplateMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: TemplateDraftOrderStrategy?,
    val players: List<TemplatePlayerSnapshot>,
)

enum class TemplateMode {
    AUCTION,
    DRAFT,
}

enum class TemplateDraftOrderStrategy {
    FIXED,
    SNAKE,
}

data class TemplatePlayerSnapshot(
    val name: String,
    val displayOrder: Int,
)

sealed class TemplateLookupException(message: String) : RuntimeException(message) {
    data class NotFound(
        val templateId: Long,
    ) : TemplateLookupException("템플릿을 찾을 수 없습니다")

    data class Invalid(
        val templateId: Long,
    ) : TemplateLookupException("유효하지 않은 템플릿입니다")
}
