package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("room_bid")
class RoomBidEntity(
    @Column override val roomId: Long,
    @Column override val round: Int,
    @Column override val teamLeaderId: String,
    @Column override val amount: Int,
    @Column override val createdAt: Instant = Instant.now(),
    @Column override val updatedAt: Instant = Instant.now(),
) : RoomBidModel {
    @Id
    var id: Long = 0L

    override val roomBidId: Long get() = id
}
