package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel

data class CreateRoomRequest(
    val templateId: Long,
    val hostNickname: String,
)

data class JoinRoomRequest(val nickname: String)

data class PlaceBidRequest(
    val teamLeaderId: String,
    val amount: Int,
)

data class PickRequest(
    val teamLeaderId: String,
    val playerName: String,
)

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

data class TeamLeaderResponse(
    val id: String,
    val nickname: String,
    val remainingBudget: Int?,
)
