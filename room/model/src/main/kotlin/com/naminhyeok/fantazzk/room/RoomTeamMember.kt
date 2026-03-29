package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomTeamMember(
    override val roomTeamMemberId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val playerName: String,
    override val assignOrder: Int,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomTeamMemberModel
