package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel

interface RoomRepository {
    fun save(room: Room): RoomModel

    fun findByCode(code: String): RoomModel?

    fun findById(roomId: Long): RoomModel?
}
