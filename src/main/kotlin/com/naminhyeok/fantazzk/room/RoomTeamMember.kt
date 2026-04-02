package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomTeamMember(
    val roomTeamMemberId: Long = 0L,
    val roomId: Long,
    val teamLeaderId: String,
    val playerName: String,
    val assignOrder: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
