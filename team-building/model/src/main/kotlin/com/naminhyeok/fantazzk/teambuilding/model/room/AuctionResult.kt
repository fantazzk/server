package com.naminhyeok.fantazzk.teambuilding.model.room

data class AuctionResult(
    val player: Player,
    val outcome: Outcome,
) {
    sealed class Outcome {
        data class Sold(val teamLeaderId: TeamLeaderId, val amount: Int) : Outcome()
        data object Passed : Outcome()
    }
}
