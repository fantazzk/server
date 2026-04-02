package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomId
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.repository.JpaRepository

@Repository
interface RoomRepository {
    fun save(room: Room): Room

    fun findByCode(code: String): Room?

    fun findById(roomId: RoomId): Room?
}

internal interface RoomJpaStore : JpaRepository<Room, Long>, RoomRepository {
    override fun save(room: Room): Room

    override fun findByCode(code: String): Room?

    override fun findById(roomId: RoomId): Room? = findById(roomId.value).orElse(null)
}
