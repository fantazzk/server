package com.naminhyeok.fantazzk.template

data class TemplateCreated(
    val templateId: Long,
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: DraftOrderStrategy?,
    val players: List<TemplatePlayerCreated>,
)

data class TemplatePlayerCreated(
    val name: String,
    val displayOrder: Int,
)
