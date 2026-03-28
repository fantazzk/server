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
    @Column override val code: String,
    @Column override val hostId: String,
    @Column override val status: RoomStatus,
    @Column override val mode: TeamBuildingMode,
    @Column override val teamCount: Int,
    @Column override val teamSize: Int,
    @Column override val budget: Int?,
    @Column override val draftOrderStrategy: DraftOrderStrategy?,
    @Column override val currentTurnIndex: Int?,
    @Column override val currentAuctionRound: Int?,
) : RoomModel {
    @Id
    var id: Long = 0L

    override val roomId: Long get() = id
}
