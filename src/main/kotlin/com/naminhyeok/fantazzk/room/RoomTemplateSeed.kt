package com.naminhyeok.fantazzk.room

data class RoomTemplateSeed(
    val mode: Mode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: DraftOrderStrategy?,
    val players: List<RoomTemplatePlayerSeed>,
) {
    enum class Mode {
        AUCTION,
        DRAFT,
    }

    enum class DraftOrderStrategy {
        FIXED,
        SNAKE,
    }
}

data class RoomTemplatePlayerSeed(
    val name: String,
    val displayOrder: Int,
)
