package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomModel

interface RoomRepository {
    fun save(room: Room): RoomModel

    fun findByCode(code: String): RoomModel?

    fun findById(roomId: Long): RoomModel?
}
