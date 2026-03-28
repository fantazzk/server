package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
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
    override fun save(player: RoomPlayer): RoomPlayerModel {
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
        return roomPlayerJdbcCrudRepository.save(entity).toModel()
    }

    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel> {
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
        return roomPlayerJdbcCrudRepository.saveAll(entities).map { it.toModel() }
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayerModel> =
        roomPlayerJdbcCrudRepository.findByRoomIdOrderByDisplayOrder(roomId).map { it.toModel() }

    override fun findFirstAvailable(roomId: Long): RoomPlayerModel? =
        roomPlayerJdbcCrudRepository
            .findFirstByRoomIdAndStatusOrderByDisplayOrder(roomId, PlayerStatus.AVAILABLE)
            ?.toModel()

    private fun RoomPlayerEntity.toModel() =
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
