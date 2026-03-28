package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel

interface RoomPlayerRepository {
    fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel>

    fun findByRoomId(roomId: Long): List<RoomPlayerModel>

    fun findFirstAvailable(roomId: Long): RoomPlayerModel?

    fun updateStatus(
        roomPlayerId: Long,
        status: PlayerStatus,
    )

    fun moveToBack(
        roomId: Long,
        roomPlayerId: Long,
    )
}
