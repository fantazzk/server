package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus

interface RoomRepository {
    fun save(room: Room): RoomModel

    fun findByCode(code: String): RoomModel?

    fun updateStatus(
        roomId: Long,
        status: RoomStatus,
    )

    fun updateCurrentTurnIndex(
        roomId: Long,
        currentTurnIndex: Int,
    )

    fun updateCurrentAuctionRound(
        roomId: Long,
        currentAuctionRound: Int,
    )
}
