package com.naminhyeok.fantazzk.room.infrastructure

import com.naminhyeok.fantazzk.room.model.RoomPlayer
import com.naminhyeok.fantazzk.room.model.RoomPlayerModel

interface RoomPlayerRepository {
    fun save(player: RoomPlayer): RoomPlayerModel

    fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel>

    fun findByRoomId(roomId: Long): List<RoomPlayerModel>

    fun findFirstAvailable(roomId: Long): RoomPlayerModel?
}
