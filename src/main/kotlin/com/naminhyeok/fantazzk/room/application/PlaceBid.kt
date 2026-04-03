package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.RoomBid
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PlaceBid(
    private val rooms: Rooms,
) {
    @Transactional
    fun place(
        code: String,
        teamLeaderId: String,
        amount: Int,
    ): RoomBid {
        val room = rooms.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = rooms.save(room.placeBid(teamLeaderId, amount))
        return savedRoom.bids.last()
    }
}
