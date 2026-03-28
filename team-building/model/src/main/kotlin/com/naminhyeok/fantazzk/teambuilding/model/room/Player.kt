package com.naminhyeok.fantazzk.teambuilding.model.room

enum class PlayerStatus {
    AVAILABLE,
    ASSIGNED,
    UNASSIGNED,
}

data class Player(
    val name: String,
    val status: PlayerStatus = PlayerStatus.AVAILABLE,
    val metadata: Map<String, String> = emptyMap(),
)
