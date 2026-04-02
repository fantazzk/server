package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.PlayerStatus
import com.naminhyeok.fantazzk.room.RoomPlayer
import org.springframework.data.repository.CrudRepository

interface RoomPlayerJdbcCrudRepository : CrudRepository<RoomPlayerEntity, Long> {
    fun findByRoomIdOrderByDisplayOrder(roomId: Long): List<RoomPlayerEntity>

    fun findFirstByRoomIdAndStatusOrderByDisplayOrder(
        roomId: Long,
        status: PlayerStatus,
    ): RoomPlayerEntity?
}

class RoomPlayerRepositoryImpl(
    private val roomPlayerJdbcCrudRepository: RoomPlayerJdbcCrudRepository,
) : RoomPlayerRepository {
    override fun save(player: RoomPlayer): RoomPlayer {
        val entity =
            RoomPlayerEntity(
                roomId = player.roomId,
                name = player.name,
                status = player.status,
                displayOrder = player.displayOrder,
                createdAt = player.createdAt,
                updatedAt = player.updatedAt,
            )
        if (player.roomPlayerId != 0L) entity.id = player.roomPlayerId
        return roomPlayerJdbcCrudRepository.save(entity).toDomain()
    }

    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayer> {
        val entities =
            players.map {
                val entity =
                    RoomPlayerEntity(
                        roomId = it.roomId,
                        name = it.name,
                        status = it.status,
                        displayOrder = it.displayOrder,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                if (it.roomPlayerId != 0L) entity.id = it.roomPlayerId
                entity
            }
        return roomPlayerJdbcCrudRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayer> =
        roomPlayerJdbcCrudRepository.findByRoomIdOrderByDisplayOrder(roomId).map { it.toDomain() }

    override fun findFirstAvailable(roomId: Long): RoomPlayer? =
        roomPlayerJdbcCrudRepository
            .findFirstByRoomIdAndStatusOrderByDisplayOrder(roomId, PlayerStatus.AVAILABLE)
            ?.toDomain()

    private fun RoomPlayerEntity.toDomain() =
        RoomPlayer(
            roomPlayerId = id,
            roomId = roomId,
            name = name,
            status = status,
            displayOrder = displayOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
