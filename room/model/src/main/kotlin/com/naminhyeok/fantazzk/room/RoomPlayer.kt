package com.naminhyeok.fantazzk.room

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
    fun assign(): RoomPlayer {
        check(isAvailable()) { "선수를 배정할 수 없습니다" }
        return copy(status = PlayerStatus.ASSIGNED)
    }

    fun moveToBack(displayOrder: Int): RoomPlayer {
        check(isAvailable()) { "선수를 뒤로 보낼 수 없습니다" }
        require(displayOrder >= 0) { "순서는 0 이상이어야 합니다" }
        require(displayOrder > this.displayOrder) { "현재 순서보다 뒤로만 이동할 수 있습니다" }
        return copy(displayOrder = displayOrder)
    }

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
