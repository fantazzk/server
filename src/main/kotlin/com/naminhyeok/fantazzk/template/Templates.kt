package com.naminhyeok.fantazzk.template

import org.jmolecules.ddd.types.Identifier

interface TemplateCatalog {
    fun getTemplateBlueprint(templateId: Long): TemplateBlueprint

    fun get(templateId: TemplateId): TemplateBlueprint = getTemplateBlueprint(templateId.value)
}

data class TemplateId(
    val value: Long,
) : Identifier

data class TemplateBlueprint(
    val templateId: Long,
    val mode: TemplateMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: TemplateDraftOrderStrategy?,
    val players: List<TemplatePlayerBlueprint>,
)

data class TemplatePlayerBlueprint(
    val name: String,
    val displayOrder: Int,
)

enum class TemplateMode {
    AUCTION,
    DRAFT,
}

enum class TemplateDraftOrderStrategy {
    FIXED,
    SNAKE,
}

sealed class TemplateCatalogException(message: String) : RuntimeException(message) {
    data class NotFound(
        val templateId: Long,
    ) : TemplateCatalogException("템플릿을 찾을 수 없습니다")

    data class Invalid(
        val templateId: Long,
    ) : TemplateCatalogException("유효하지 않은 템플릿입니다")
}
