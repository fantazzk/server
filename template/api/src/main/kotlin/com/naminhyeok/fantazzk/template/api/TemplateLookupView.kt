package com.naminhyeok.fantazzk.template.api

data class TemplatePlayerView(
    val name: String,
    val displayOrder: Int,
)

data class TemplateView(
    val templateId: Long,
    val mode: TemplateMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: TemplateDraftStrategy?,
    val players: List<TemplatePlayerView>,
)

enum class TemplateMode {
    AUCTION,
    DRAFT,
}

enum class TemplateDraftStrategy {
    SNAKE,
    FIXED,
}
