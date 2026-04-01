package com.naminhyeok.fantazzk.room

data class RoomCreated(
    val roomId: Long,
    val code: String,
    val status: RoomStatus,
    val hostLeader: LeaderSnapshot,
)

data class RoomJoined(
    val roomId: Long,
    val code: String,
    val leader: LeaderSnapshot,
)

data class LeaderSnapshot(
    val teamLeaderId: String,
    val nickname: String,
    val remainingBudget: Int?,
)

data class RoomStarted(
    val roomId: Long,
    val code: String,
    val mode: Mode,
) {
    enum class Mode {
        AUCTION,
        DRAFT,
    }
}

data class AuctionSettled(
    val roomId: Long,
    val code: String,
    val playerName: String,
    val outcome: AuctionOutcome,
)

data class DraftPickCompleted(
    val roomId: Long,
    val code: String,
    val playerName: String,
    val teamLeaderId: String,
)

data class RoomCompleted(
    val roomId: Long,
    val code: String,
    val mode: RoomStarted.Mode,
)
