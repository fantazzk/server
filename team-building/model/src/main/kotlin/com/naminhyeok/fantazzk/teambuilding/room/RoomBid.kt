package com.naminhyeok.fantazzk.teambuilding.room

import java.time.Instant

data class RoomBid(
    override val roomBidId: Long = 0L,
    override val roomId: Long,
    override val round: Int,
    override val teamLeaderId: String,
    override val amount: Int,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomBidModel
