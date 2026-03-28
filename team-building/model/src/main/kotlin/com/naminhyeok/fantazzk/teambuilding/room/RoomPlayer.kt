package com.naminhyeok.fantazzk.teambuilding.room

import java.time.Instant

data class RoomPlayer(
    override val roomPlayerId: Long = 0L,
    override val roomId: Long,
    override val name: String,
    override val status: PlayerStatus = PlayerStatus.AVAILABLE,
    override val displayOrder: Int,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomPlayerModel {
    companion object {
        fun from(model: RoomPlayerModel): RoomPlayer =
            RoomPlayer(
                roomPlayerId = model.roomPlayerId,
                roomId = model.roomId,
                name = model.name,
                status = model.status,
                displayOrder = model.displayOrder,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }
}
