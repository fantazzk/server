package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomId
import org.jmolecules.ddd.annotation.Repository

@Repository
interface RoomRepository {
    fun save(room: Room): Room

    fun findByCode(code: String): Room?

    fun findById(roomId: RoomId): Room?
}
