package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_team_member")
class RoomTeamMemberEntity(
    @Column("room_id") override val roomId: Long,
    @Column("team_leader_id") override val teamLeaderId: String,
    @Column("player_name") override val playerName: String,
    @Column("assign_order") override val assignOrder: Int,
) : RoomTeamMemberModel {
    @Id
    var id: Long = 0L

    override val roomTeamMemberId: Long get() = id
}
