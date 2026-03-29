package com.naminhyeok.fantazzk.room

interface RoomTeamLeaderProps {
    val roomId: Long
    val teamLeaderId: String
    val nickname: String
    val remainingBudget: Int?
}
