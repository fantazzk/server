package com.naminhyeok.fantazzk.room

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
}
