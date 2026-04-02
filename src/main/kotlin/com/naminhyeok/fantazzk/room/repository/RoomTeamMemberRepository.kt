package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomTeamMember
import org.jmolecules.ddd.annotation.Repository

@Repository
interface RoomTeamMemberRepository {
    fun save(member: RoomTeamMember): RoomTeamMember

    fun findByRoomId(roomId: Long): List<RoomTeamMember>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMember>

    fun countByRoomId(roomId: Long): Int

    fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int
}
