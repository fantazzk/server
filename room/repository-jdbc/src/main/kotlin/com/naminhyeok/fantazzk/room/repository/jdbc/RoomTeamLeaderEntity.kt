package com.naminhyeok.fantazzk.room.repository.jdbc

import com.naminhyeok.fantazzk.room.model.RoomTeamLeaderModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_team_leader")
class RoomTeamLeaderEntity(
    @Column override val roomId: Long,
    @Column override val teamLeaderId: String,
    @Column override val nickname: String,
    @Column override val remainingBudget: Int?,
    @Column override val createdAt: Instant = Instant.now(),
    @Column override val updatedAt: Instant = Instant.now(),
) : RoomTeamLeaderModel {
    @Id
    var id: Long = 0L

    override val roomTeamLeaderId: Long get() = id
}
