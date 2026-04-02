package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import org.springframework.data.repository.CrudRepository

interface RoomJdbcCrudRepository : CrudRepository<RoomEntity, Long> {
    fun findByCode(code: String): RoomEntity?
}

class RoomRepositoryImpl(
    private val roomJdbcCrudRepository: RoomJdbcCrudRepository,
) : RoomRepository {
    override fun save(room: Room): Room {
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
        return roomJdbcCrudRepository.save(entity).toDomain()
    }

    override fun findByCode(code: String): Room? = roomJdbcCrudRepository.findByCode(code)?.toDomain()

    override fun findById(roomId: Long): Room? = roomJdbcCrudRepository.findById(roomId).orElse(null)?.toDomain()

    private fun RoomEntity.toDomain() =
        Room(
            roomId = id,
            code = code,
            hostId = hostId,
            status = status,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = if (mode == TeamBuildingMode.AUCTION) budget else null,
            draftOrderStrategy = if (mode == TeamBuildingMode.DRAFT) draftOrderStrategy else null,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
