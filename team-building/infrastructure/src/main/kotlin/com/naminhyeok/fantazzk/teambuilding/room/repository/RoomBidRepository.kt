package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel

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

    fun countByRoomId(roomId: Long): Int
}
