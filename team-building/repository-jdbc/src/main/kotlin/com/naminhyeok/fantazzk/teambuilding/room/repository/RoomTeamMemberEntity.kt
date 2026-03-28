package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_team_member")
class RoomTeamMemberEntity(
    @Column override val roomId: Long,
    @Column override val teamLeaderId: String,
    @Column override val playerName: String,
    @Column override val assignOrder: Int,
) : RoomTeamMemberModel {
    @Id
    var id: Long = 0L

    override val roomTeamMemberId: Long get() = id
}
