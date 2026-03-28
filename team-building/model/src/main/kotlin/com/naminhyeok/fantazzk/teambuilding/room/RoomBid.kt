package com.naminhyeok.fantazzk.teambuilding.room

data class RoomBid(
    override val roomBidId: Long = 0L,
    override val roomId: Long,
    override val round: Int,
    override val teamLeaderId: String,
    override val amount: Int,
) : RoomBidModel
