package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomPlayer
import org.jmolecules.ddd.annotation.Repository

@Repository
interface RoomPlayerRepository {
    fun save(player: RoomPlayer): RoomPlayer

    fun saveAll(players: List<RoomPlayer>): List<RoomPlayer>

    fun findByRoomId(roomId: Long): List<RoomPlayer>

    fun findFirstAvailable(roomId: Long): RoomPlayer?
}
