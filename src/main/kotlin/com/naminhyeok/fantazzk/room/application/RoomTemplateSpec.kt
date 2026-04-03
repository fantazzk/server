package com.naminhyeok.fantazzk.room.application

internal data class RoomTemplateSpec(
    val mode: Mode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: DraftOrderStrategy?,
    val players: List<Player>,
) {
    enum class Mode {
        AUCTION,
        DRAFT,
    }

    enum class DraftOrderStrategy {
        FIXED,
        SNAKE,
    }

    data class Player(
        val name: String,
        val displayOrder: Int,
    )
}
