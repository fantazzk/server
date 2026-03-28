package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_player")
class RoomPlayerEntity(
    @Column override val roomId: Long,
    @Column override val name: String,
    @Column override val status: PlayerStatus,
    @Column override val displayOrder: Int,
    @Column override val createdAt: Instant = Instant.now(),
    @Column override val updatedAt: Instant = Instant.now(),
) : RoomPlayerModel {
    @Id
    var id: Long = 0L

    override val roomPlayerId: Long get() = id
}
