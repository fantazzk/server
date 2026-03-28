package com.naminhyeok.fantazzk.teambuilding.room

data class RoomTeamMember(
    override val roomTeamMemberId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val playerName: String,
    override val assignOrder: Int,
) : RoomTeamMemberModel
