package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_player")
class RoomPlayerEntity(
    @Column("room_id") override val roomId: Long,
    @Column("name") override val name: String,
    @Column("status") val statusValue: String,
    @Column("display_order") override val displayOrder: Int,
) : RoomPlayerModel {
    @Id
    var id: Long = 0L

    override val roomPlayerId: Long get() = id
    override val status: PlayerStatus get() = PlayerStatus.valueOf(statusValue)
}
