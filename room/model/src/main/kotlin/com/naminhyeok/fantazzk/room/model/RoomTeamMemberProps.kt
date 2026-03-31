package com.naminhyeok.fantazzk.room.model

interface RoomTeamMemberProps {
    val roomId: Long
    val teamLeaderId: String
    val playerName: String
    val assignOrder: Int
}
