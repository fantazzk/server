package com.naminhyeok.fantazzk.room.repository.jdbc

import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomModel
import org.springframework.data.repository.CrudRepository

interface RoomJdbcCrudRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}

class RoomRepositoryImpl(
    private val roomJdbcCrudRepository: RoomJdbcCrudRepository,
) : RoomRepository {
    override fun save(room: Room): RoomModel {
        val entity =
            RoomEntity(
                code = room.code,
                hostId = room.hostId,
                status = room.status,
                mode = room.mode,
                teamCount = room.teamCount,
                teamSize = room.teamSize,
                budget = room.budget,
                draftOrderStrategy = room.draftOrderStrategy,
                currentTurnIndex = room.currentTurnIndex,
                currentAuctionRound = room.currentAuctionRound,
                createdAt = room.createdAt,
                updatedAt = room.updatedAt,
            )
        if (room.roomId != 0L) entity.id = room.roomId
        return roomJdbcCrudRepository.save(entity).toModel()
    }

    override fun findByCode(code: String): RoomModel? = roomJdbcCrudRepository.findByCode(code)?.toModel()

    override fun findById(roomId: Long): RoomModel? = roomJdbcCrudRepository.findById(roomId).orElse(null)?.toModel()

    private fun RoomEntity.toModel() = Room.from(this)
}
