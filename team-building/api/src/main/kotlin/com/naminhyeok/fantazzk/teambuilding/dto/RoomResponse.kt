package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel

data class RoomResponse(
    val code: String,
    val status: RoomStatus,
    val teamLeaders: List<TeamLeaderResponse>,
) {
    companion object {
        fun from(
            room: RoomModel,
            leaders: List<RoomTeamLeaderModel>,
        ): RoomResponse =
            RoomResponse(
                code = room.code,
                status = room.status,
                teamLeaders = leaders.map { TeamLeaderResponse(it.teamLeaderId, it.nickname, it.remainingBudget) },
            )
    }
}
