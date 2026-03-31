package com.naminhyeok.fantazzk.room.infrastructure

import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomModel

interface RoomRepository {
    fun save(room: Room): RoomModel

    fun findByCode(code: String): RoomModel?

    fun findById(roomId: Long): RoomModel?
}
