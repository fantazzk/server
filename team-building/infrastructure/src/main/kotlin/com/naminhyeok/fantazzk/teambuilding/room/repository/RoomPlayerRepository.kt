package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel

interface RoomPlayerRepository {
    fun save(player: RoomPlayer): RoomPlayerModel

    fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel>

    fun findByRoomId(roomId: Long): List<RoomPlayerModel>

    fun findFirstAvailable(roomId: Long): RoomPlayerModel?
}
