package com.naminhyeok.fantazzk.room.dto

import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamLeaderModel

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
