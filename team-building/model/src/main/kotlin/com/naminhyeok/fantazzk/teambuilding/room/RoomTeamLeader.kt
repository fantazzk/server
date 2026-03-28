package com.naminhyeok.fantazzk.teambuilding.room

data class RoomTeamLeader(
    override val roomTeamLeaderId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val nickname: String,
    override val remainingBudget: Int? = null,
) : RoomTeamLeaderModel {
    companion object {
        fun from(model: RoomTeamLeaderModel): RoomTeamLeader =
            RoomTeamLeader(
                roomTeamLeaderId = model.roomTeamLeaderId,
                roomId = model.roomId,
                teamLeaderId = model.teamLeaderId,
                nickname = model.nickname,
                remainingBudget = model.remainingBudget,
            )
    }
}
