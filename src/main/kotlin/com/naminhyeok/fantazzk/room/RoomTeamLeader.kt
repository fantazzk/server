package com.naminhyeok.fantazzk.room

import java.time.Instant

data class RoomTeamLeader(
    val roomTeamLeaderId: Long = 0L,
    val roomId: Long,
    val teamLeaderId: String,
    val nickname: String,
    val remainingBudget: Int? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    fun requireCanBid(amount: Int) {
        budgetState().requireCanBid(amount)
    }

    fun spend(amount: Int): RoomTeamLeader =
        copy(
            remainingBudget = budgetState().spend(amount).remainingBudget,
        )

    private fun budgetState(): BudgetState = BudgetState.requireFrom(remainingBudget)
}

fun RoomTeamLeader.validateBudget(amount: Int) = requireCanBid(amount)
