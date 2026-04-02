package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomBid
import org.jmolecules.ddd.annotation.Repository

@Repository
interface RoomBidRepository {
    fun save(bid: RoomBid): RoomBid

    fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBid>

    fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBid?
}
