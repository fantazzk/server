package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomBid(
    val roomBidId: Long = 0L,
    val roomId: Long,
    val round: Int,
    val teamLeaderId: String,
    val amount: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
