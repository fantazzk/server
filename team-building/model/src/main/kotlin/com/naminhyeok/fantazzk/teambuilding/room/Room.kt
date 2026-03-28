package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import java.time.Instant

data class Room(
    override val roomId: Long = 0L,
    override val code: String,
    override val hostId: String,
    override val status: RoomStatus,
    override val mode: TeamBuildingMode,
    override val teamCount: Int,
    override val teamSize: Int,
    override val budget: Int? = null,
    override val draftOrderStrategy: DraftOrderStrategy? = null,
    override val currentTurnIndex: Int? = null,
    override val currentAuctionRound: Int? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomModel {
    companion object {
        fun from(model: RoomModel): Room =
            Room(
                roomId = model.roomId,
                code = model.code,
                hostId = model.hostId,
                status = model.status,
                mode = model.mode,
                teamCount = model.teamCount,
                teamSize = model.teamSize,
                budget = model.budget,
                draftOrderStrategy = model.draftOrderStrategy,
                currentTurnIndex = model.currentTurnIndex,
                currentAuctionRound = model.currentAuctionRound,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }
}
