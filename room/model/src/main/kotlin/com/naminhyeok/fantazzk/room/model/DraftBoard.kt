package com.naminhyeok.fantazzk.room.model

data class DraftBoard(
    val teamLeaderIds: List<String>,
    val strategy: DraftOrderStrategy,
    val picksPerTeam: Int,
) {
    fun pickOrder(): List<String> {
        val reversedOrder = teamLeaderIds.asReversed()
        return (0 until picksPerTeam).flatMap { round ->
            when (strategy) {
                DraftOrderStrategy.SNAKE -> if (round % 2 == 0) teamLeaderIds else reversedOrder
                DraftOrderStrategy.FIXED -> teamLeaderIds
            }
        }
    }

    fun currentTeamLeader(turnIndex: Int): String {
        require(turnIndex >= 0) { "드래프트 턴은 0 이상이어야 합니다" }
        val order = pickOrder()
        check(turnIndex < order.size) { "드래프트가 이미 종료되었습니다" }
        return order[turnIndex]
    }

    fun requireTurnOwner(
        turnIndex: Int,
        teamLeaderId: String,
    ) {
        check(currentTeamLeader(turnIndex) == teamLeaderId) { "현재 턴이 아닙니다" }
    }

    fun settlePick(
        turnIndex: Int,
        assignedCountAfterPick: Int,
    ): DraftPickSettlement =
        DraftPickSettlement(
            nextTurnIndex = turnIndex + 1,
            completed = assignedCountAfterPick >= totalPickCount(),
        )

    private fun totalPickCount(): Int = teamLeaderIds.size * picksPerTeam
}

data class DraftPickSettlement(
    val nextTurnIndex: Int,
    val completed: Boolean,
)
