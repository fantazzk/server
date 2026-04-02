package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomId
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Repository
interface RoomRepository {
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
) : RoomRepository {
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
