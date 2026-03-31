package com.naminhyeok.fantazzk.room.model

interface RoomTeamLeaderProps {
    val roomId: Long
    val teamLeaderId: String
    val nickname: String
    val remainingBudget: Int?
}
