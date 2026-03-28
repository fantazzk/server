package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_team_leader")
class RoomTeamLeaderEntity(
    @Column("room_id") override val roomId: Long,
    @Column("team_leader_id") override val teamLeaderId: String,
    @Column("nickname") override val nickname: String,
    @Column("remaining_budget") override val remainingBudget: Int?,
) : RoomTeamLeaderModel {
    @Id
    var id: Long = 0L

    override val roomTeamLeaderId: Long get() = id
}
