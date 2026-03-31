package com.naminhyeok.fantazzk.room.infrastructure

import com.naminhyeok.fantazzk.room.model.RoomTeamLeader
import com.naminhyeok.fantazzk.room.model.RoomTeamLeaderModel

interface RoomTeamLeaderRepository {
    fun save(leader: RoomTeamLeader): RoomTeamLeaderModel

    fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel?
}
