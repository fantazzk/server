package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomTeamLeader
import org.jmolecules.ddd.annotation.Repository

@Repository
interface RoomTeamLeaderRepository {
    fun save(leader: RoomTeamLeader): RoomTeamLeader

    fun findByRoomId(roomId: Long): List<RoomTeamLeader>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeader?
}
