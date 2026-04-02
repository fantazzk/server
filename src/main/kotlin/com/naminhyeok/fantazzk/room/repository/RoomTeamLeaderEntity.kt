package com.naminhyeok.fantazzk.room.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_team_leader")
class RoomTeamLeaderEntity(
    @Column val roomId: Long,
    @Column val teamLeaderId: String,
    @Column val nickname: String,
    @Column val remainingBudget: Int?,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
