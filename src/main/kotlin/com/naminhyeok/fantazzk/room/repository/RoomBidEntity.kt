package com.naminhyeok.fantazzk.room.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_bid")
class RoomBidEntity(
    @Column val roomId: Long,
    @Column val round: Int,
    @Column val teamLeaderId: String,
    @Column val amount: Int,
    @Column val createdAt: Instant = Instant.now(),
    @Column val updatedAt: Instant = Instant.now(),
) {
    @Id
    var id: Long = 0L
}
