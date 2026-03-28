package com.naminhyeok.fantazzk.teambuilding.room

interface RoomBidIdentity {
    val roomBidId: Long
}

interface RoomBidProps {
    val roomId: Long
    val round: Int
    val teamLeaderId: String
    val amount: Int
}

interface RoomBidModel : RoomBidIdentity, RoomBidProps
