package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomCode
import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.domain.Room
import org.jmolecules.ddd.types.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface Rooms : Repository<Room, RoomId> {
    fun save(room: Room): Room

    fun findByCode(code: String): Room?

    fun findById(roomId: RoomId): Room?
}

internal interface RoomJpaStore : JpaRepository<Room, UUID> {
    fun findByRoomCode(roomCode: RoomCode): Room?
}

@Component
class RoomRepositoryAdapter internal constructor(
    private val store: RoomJpaStore,
) : Rooms {
    @Transactional
    override fun save(room: Room): Room = store.save(room)

    @Transactional(readOnly = true)
    override fun findByCode(code: String): Room? = store.findByRoomCode(RoomCode.of(code))?.also(::initializeGraph)

    @Transactional(readOnly = true)
    override fun findById(roomId: RoomId): Room? = store.findById(roomId.value).orElse(null)?.also(::initializeGraph)

    private fun initializeGraph(room: Room) {
        room.players.size
        room.leaders.size
        room.members.size
        room.bidHistory().size
    }
}
