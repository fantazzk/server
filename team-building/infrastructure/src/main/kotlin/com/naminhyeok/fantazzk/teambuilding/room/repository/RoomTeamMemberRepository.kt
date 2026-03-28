package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel

interface RoomTeamMemberRepository {
    fun save(member: RoomTeamMember): RoomTeamMemberModel

    fun findByRoomId(roomId: Long): List<RoomTeamMemberModel>

    fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel>

    fun countByRoomId(roomId: Long): Int
}
