package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.PlayerStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_player")
class RoomPlayerEntity(
    @Column val roomId: Long,
    @Column val name: String,
    @Column val status: PlayerStatus,
    @Column val displayOrder: Int,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
