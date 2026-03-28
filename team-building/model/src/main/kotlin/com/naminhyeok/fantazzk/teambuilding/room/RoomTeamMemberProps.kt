package com.naminhyeok.fantazzk.teambuilding.room

interface RoomTeamMemberProps {
    val roomId: Long
    val teamLeaderId: String
    val playerName: String
    val assignOrder: Int
}
