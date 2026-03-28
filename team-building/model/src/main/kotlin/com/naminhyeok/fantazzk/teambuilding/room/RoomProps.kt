package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

interface RoomProps {
    val code: String
    val hostId: String
    val status: RoomStatus
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int
    val budget: Int?
    val draftOrderStrategy: DraftOrderStrategy?
    val currentTurnIndex: Int?
    val currentAuctionRound: Int?

    fun isWaiting(): Boolean = status == RoomStatus.WAITING

    fun isInProgress(): Boolean = status == RoomStatus.IN_PROGRESS

    fun isAuction(): Boolean = mode == TeamBuildingMode.AUCTION

    fun isDraft(): Boolean = mode == TeamBuildingMode.DRAFT

    val picksPerTeam: Int get() = teamSize - 1
}
