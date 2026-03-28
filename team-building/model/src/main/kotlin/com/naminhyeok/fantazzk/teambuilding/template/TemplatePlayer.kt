package com.naminhyeok.fantazzk.teambuilding.template

data class TemplatePlayer(
    override val templatePlayerId: Long = 0L,
    override val templateId: Long,
    override val name: String,
    override val displayOrder: Int,
) : TemplatePlayerModel
