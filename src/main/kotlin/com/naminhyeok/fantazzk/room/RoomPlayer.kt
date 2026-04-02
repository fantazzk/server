package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomPlayer(
    val roomPlayerId: Long = 0L,
    val roomId: Long,
    val name: String,
    val status: PlayerStatus = PlayerStatus.AVAILABLE,
    val displayOrder: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    fun assign(): RoomPlayer {
        check(status == PlayerStatus.AVAILABLE) { "선수를 배정할 수 없습니다" }
        return copy(status = PlayerStatus.ASSIGNED)
    }

    fun moveToBack(displayOrder: Int): RoomPlayer {
        check(status == PlayerStatus.AVAILABLE) { "선수를 뒤로 보낼 수 없습니다" }
        require(displayOrder >= 0) { "순서는 0 이상이어야 합니다" }
        require(displayOrder > this.displayOrder) { "현재 순서보다 뒤로만 이동할 수 있습니다" }
        return copy(displayOrder = displayOrder)
    }

}

fun RoomPlayer.isAvailable(): Boolean = status == PlayerStatus.AVAILABLE
