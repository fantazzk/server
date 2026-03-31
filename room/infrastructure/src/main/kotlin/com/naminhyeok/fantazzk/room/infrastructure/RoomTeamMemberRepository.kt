package com.naminhyeok.fantazzk.room.infrastructure

import com.naminhyeok.fantazzk.room.model.RoomTeamMember
import com.naminhyeok.fantazzk.room.model.RoomTeamMemberModel

interface RoomTeamMemberRepository {
    fun save(member: RoomTeamMember): RoomTeamMemberModel

    fun findByRoomId(roomId: Long): List<RoomTeamMemberModel>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel>

    fun countByRoomId(roomId: Long): Int

    fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int
}
