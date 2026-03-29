package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomBidModel

interface RoomBidRepository {
    fun save(bid: RoomBid): RoomBidModel

    fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel>

    fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBidModel?
}
