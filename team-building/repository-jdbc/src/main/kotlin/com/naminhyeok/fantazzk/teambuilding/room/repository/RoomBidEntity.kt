package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room_bid")
class RoomBidEntity(
    @Column("room_id") override val roomId: Long,
    @Column("round") override val round: Int,
    @Column("team_leader_id") override val teamLeaderId: String,
    @Column("amount") override val amount: Int,
) : RoomBidModel {
    @Id
    var id: Long = 0L

    override val roomBidId: Long get() = id
}
