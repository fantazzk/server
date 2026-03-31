package com.naminhyeok.fantazzk.room.model

import java.time.Instant

data class RoomTeamLeader(
    override val roomTeamLeaderId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val nickname: String,
    override val remainingBudget: Int? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : RoomTeamLeaderModel {
    fun requireCanBid(amount: Int) {
        budgetState().requireCanBid(amount)
    }

    fun spend(amount: Int): RoomTeamLeader =
        copy(
            remainingBudget = budgetState().spend(amount).remainingBudget,
        )

    companion object {
        fun from(model: RoomTeamLeaderModel): RoomTeamLeader =
            RoomTeamLeader(
                roomTeamLeaderId = model.roomTeamLeaderId,
                roomId = model.roomId,
                teamLeaderId = model.teamLeaderId,
                nickname = model.nickname,
                remainingBudget = model.remainingBudget,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
    }

    private fun budgetState(): BudgetState = BudgetState.requireFrom(remainingBudget)
}
