package com.naminhyeok.fantazzk.template

data class TemplateBlueprint(
    val templateId: TemplateId,
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
