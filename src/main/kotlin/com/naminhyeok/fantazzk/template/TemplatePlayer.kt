package com.naminhyeok.fantazzk.template

import java.time.Instant

data class TemplatePlayer(
    val templatePlayerId: Long = 0L,
    val templateId: Long,
    val name: String,
    val displayOrder: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    companion object {
        fun from(model: TemplatePlayerModel): TemplatePlayer =
            TemplatePlayer(
                templatePlayerId = model.templatePlayerId,
                templateId = model.templateId,
                name = model.name,
                displayOrder = model.displayOrder,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }
}
