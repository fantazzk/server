package com.naminhyeok.fantazzk.room

data class AuctionRound(
    val round: Int,
    val highestBid: RoomBid? = null,
) {
    init {
        require(round > 0) { "경매 라운드는 1 이상이어야 합니다" }
    }

    fun requireHigherBid(amount: Int) {
        require(amount > highestAmount()) { "현재 최고가보다 높아야 합니다" }
    }

    fun requireRosterCapacity(
        currentMemberCount: Int,
        picksPerTeam: Int,
    ) {
        check(currentMemberCount < picksPerTeam) { "팀장의 팀원 정원이 가득 찼습니다" }
    }

    fun settle(
        playerName: String,
        assignedCountAfterSettlement: Int,
        totalRequired: Int,
    ): AuctionRoundSettlement {
        val nextRound = round + 1
        return if (highestBid != null) {
            AuctionRoundSettlement(
                playerName = playerName,
                outcome = AuctionOutcome.SOLD,
                nextRound = nextRound,
                completed = assignedCountAfterSettlement >= totalRequired,
                winningBid = highestBid,
            )
        } else {
            AuctionRoundSettlement(
                playerName = playerName,
                outcome = AuctionOutcome.PASSED,
                nextRound = nextRound,
                completed = false,
                winningBid = null,
            )
        }
    }

    private fun highestAmount(): Int = highestBid?.amount ?: 0
}

data class AuctionRoundSettlement(
    val playerName: String,
    val outcome: AuctionOutcome,
    val nextRound: Int,
    val completed: Boolean,
    val winningBid: RoomBid?,
)
