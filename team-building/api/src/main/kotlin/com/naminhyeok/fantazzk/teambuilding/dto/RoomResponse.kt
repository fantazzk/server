package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus

data class RoomResponse(
    val code: String,
    val status: RoomStatus,
    val teamLeaders: List<TeamLeaderResponse>,
    val currentTarget: String?,
    val result: List<TeamResponse>?,
) {
    companion object {
        fun from(room: Room): RoomResponse =
            RoomResponse(
                code = room.code,
                status = room.status,
                teamLeaders =
                    room.teamLeaders.values.map {
                        TeamLeaderResponse(
                            id = it.id.value,
                            nickname = it.nickname,
                            remainingBudget = it.remainingBudget,
                            teamSize = it.team.size,
                        )
                    },
                currentTarget = room.playerPool.currentTarget()?.name,
                result =
                    room.result?.teams?.map { team ->
                        TeamResponse(
                            leaderNickname = team.leaderNickname,
                            members = team.members.map { it.name },
                        )
                    },
            )
    }
}

data class TeamLeaderResponse(
    val id: String,
    val nickname: String,
    val remainingBudget: Int?,
    val teamSize: Int,
)

data class TeamResponse(
    val leaderNickname: String,
    val members: List<String>,
)
