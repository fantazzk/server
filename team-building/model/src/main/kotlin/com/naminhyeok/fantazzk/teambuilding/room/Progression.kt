package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy

sealed class Progression {
    data class Auction(
        val currentBids: List<Bid> = emptyList(),
        val history: List<AuctionResult> = emptyList(),
    ) : Progression() {
        fun addBid(bid: Bid): Auction = copy(currentBids = currentBids + bid)

        fun highestBid(): Bid? = currentBids.maxByOrNull { it.amount }

        fun addResult(result: AuctionResult): Auction = copy(currentBids = emptyList(), history = history + result)
    }

    data class Draft(
        val pickOrder: List<TeamLeaderId>,
        val currentTurnIndex: Int = 0,
        val history: List<Pick> = emptyList(),
    ) : Progression() {
        fun currentTurn(): TeamLeaderId = pickOrder[currentTurnIndex]

        fun advanceTurn(): Draft = copy(currentTurnIndex = currentTurnIndex + 1)

        fun addPick(pick: Pick): Draft = copy(history = history + pick)

        fun isFinished(): Boolean = currentTurnIndex >= pickOrder.size

        companion object {
            fun generatePickOrder(
                teamLeaders: List<TeamLeaderId>,
                strategy: DraftOrderStrategy,
                picksPerTeam: Int,
            ): List<TeamLeaderId> {
                val reversed = teamLeaders.reversed()
                return (0 until picksPerTeam).flatMap { round ->
                    when (strategy) {
                        DraftOrderStrategy.SNAKE ->
                            if (round % 2 == 0) teamLeaders else reversed
                        DraftOrderStrategy.FIXED -> teamLeaders
                    }
                }
            }
        }
    }
}
