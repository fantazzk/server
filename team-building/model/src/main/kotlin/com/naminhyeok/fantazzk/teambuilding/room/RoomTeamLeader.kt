package com.naminhyeok.fantazzk.teambuilding.room

import java.time.Instant

data class RoomTeamLeader(
    override val roomTeamLeaderId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val nickname: String,
    override val remainingBudget: Int? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomTeamLeaderModel {
    companion object {
        fun from(model: RoomTeamLeaderModel): RoomTeamLeader =
            RoomTeamLeader(
                roomTeamLeaderId = model.roomTeamLeaderId,
                roomId = model.roomId,
                teamLeaderId = model.teamLeaderId,
                nickname = model.nickname,
                remainingBudget = model.remainingBudget,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }
}
