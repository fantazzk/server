package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel

interface RoomTeamLeaderRepository {
    fun save(leader: RoomTeamLeader): RoomTeamLeaderModel

    fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel?

    fun updateRemainingBudget(
        roomTeamLeaderId: Long,
        remainingBudget: Int,
    )
}
