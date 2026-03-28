package com.naminhyeok.fantazzk.teambuilding.room

interface RoomTeamLeaderIdentity {
    val roomTeamLeaderId: Long
}

interface RoomTeamLeaderProps {
    val roomId: Long
    val teamLeaderId: String
    val nickname: String
    val remainingBudget: Int?
}

interface RoomTeamLeaderModel : RoomTeamLeaderIdentity, RoomTeamLeaderProps
