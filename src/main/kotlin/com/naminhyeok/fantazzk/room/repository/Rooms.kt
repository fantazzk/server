package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.domain.RoomId
import org.jmolecules.ddd.types.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.jmolecules.ddd.annotation.Repository as DddRepository

@DddRepository
interface Rooms : Repository<Room, RoomId> {
    fun save(room: Room): Room

    fun findByCode(code: String): Room?

    fun findById(roomId: RoomId): Room?
}

internal interface RoomJpaStore : JpaRepository<Room, Long> {
    fun findByCode(code: String): Room?

    fun findByPersistentId(roomId: Long): Room?
}

@Component
class RoomRepositoryAdapter internal constructor(
    private val store: RoomJpaStore,
) : Rooms {
    @Transactional
    override fun save(room: Room): Room = store.save(room)

    @Transactional(readOnly = true)
    override fun findByCode(code: String): Room? = store.findByCode(code)?.also(::initializeGraph)

    @Transactional(readOnly = true)
    override fun findById(roomId: RoomId): Room? = store.findByPersistentId(roomId.value)?.also(::initializeGraph)

    private fun initializeGraph(room: Room) {
        room.players.size
        room.leaders.size
        room.members.size
        room.bidHistory().size
    }
}
