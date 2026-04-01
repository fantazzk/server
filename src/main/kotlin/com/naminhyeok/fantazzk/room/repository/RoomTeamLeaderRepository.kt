package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.RoomTeamLeaderModel

interface RoomTeamLeaderRepository {
    fun save(leader: RoomTeamLeader): RoomTeamLeaderModel

    fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel?
}
