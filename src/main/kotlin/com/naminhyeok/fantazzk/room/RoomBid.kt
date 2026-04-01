package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomBid(
    override val roomBidId: Long = 0L,
    override val roomId: Long,
    override val round: Int,
    override val teamLeaderId: String,
    override val amount: Int,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomBidModel {
    companion object {
        fun from(model: RoomBidModel): RoomBid =
            RoomBid(
                roomBidId = model.roomBidId,
                roomId = model.roomId,
                round = model.round,
                teamLeaderId = model.teamLeaderId,
                amount = model.amount,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }
}
