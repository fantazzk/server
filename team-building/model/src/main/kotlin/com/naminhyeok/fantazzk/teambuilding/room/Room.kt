package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

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
) : RoomModel
