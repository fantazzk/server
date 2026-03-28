package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomId

interface RoomRepository {
    fun save(room: Room): Room

    fun findByCode(code: String): Room?

    fun findById(id: RoomId): Room?
}
