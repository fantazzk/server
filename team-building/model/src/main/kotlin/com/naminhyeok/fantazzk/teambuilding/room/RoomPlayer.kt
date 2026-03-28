package com.naminhyeok.fantazzk.teambuilding.room

data class RoomPlayer(
    override val roomPlayerId: Long = 0L,
    override val roomId: Long,
    override val name: String,
    override val status: PlayerStatus = PlayerStatus.AVAILABLE,
    override val displayOrder: Int,
) : RoomPlayerModel {
    fun isAvailable(): Boolean = status == PlayerStatus.AVAILABLE

    companion object {
        fun from(model: RoomPlayerModel): RoomPlayer =
            RoomPlayer(
                roomPlayerId = model.roomPlayerId,
                roomId = model.roomId,
                name = model.name,
                status = model.status,
                displayOrder = model.displayOrder,
            )
    }
}
