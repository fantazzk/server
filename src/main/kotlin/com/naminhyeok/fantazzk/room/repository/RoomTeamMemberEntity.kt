package com.naminhyeok.fantazzk.room.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_team_member")
class RoomTeamMemberEntity(
    @Column val roomId: Long,
    @Column val teamLeaderId: String,
    @Column val playerName: String,
    @Column val assignOrder: Int,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
