package com.naminhyeok.fantazzk.teambuilding.template

import java.time.Instant

data class TemplatePlayer(
    override val templatePlayerId: Long = 0L,
    override val templateId: Long,
    override val name: String,
    override val displayOrder: Int,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : TemplatePlayerModel
