package com.naminhyeok.fantazzk.room.infrastructure

import com.naminhyeok.fantazzk.room.model.RoomBid
import com.naminhyeok.fantazzk.room.model.RoomBidModel

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
