package com.naminhyeok.fantazzk.teambuilding.room

interface RoomTeamMemberIdentity {
    companion object

    val roomTeamMemberId: Long
}

interface RoomTeamMemberProps {
    val roomId: Long
    val teamLeaderId: String
    val playerName: String
    val assignOrder: Int
}

interface RoomTeamMemberModel : RoomTeamMemberIdentity, RoomTeamMemberProps
