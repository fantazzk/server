package com.naminhyeok.fantazzk.teambuilding.room.repository

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("room")
class RoomEntity(
    @Column("code") override val code: String,
    @Column("host_id") override val hostId: String,
    @Column("status") val statusValue: String,
    @Column("mode") val modeValue: String,
    @Column("team_count") override val teamCount: Int,
    @Column("team_size") override val teamSize: Int,
    @Column("budget") override val budget: Int?,
    @Column("draft_order_strategy") val draftOrderStrategyValue: String?,
    @Column("current_turn_index") override val currentTurnIndex: Int?,
    @Column("current_auction_round") override val currentAuctionRound: Int?,
) : RoomModel {
    @Id
    var id: Long = 0L

    override val roomId: Long get() = id
    override val status: RoomStatus get() = RoomStatus.valueOf(statusValue)
    override val mode: TeamBuildingMode get() = TeamBuildingMode.valueOf(modeValue)
    override val draftOrderStrategy: DraftOrderStrategy? get() = draftOrderStrategyValue?.let { DraftOrderStrategy.valueOf(it) }
}
