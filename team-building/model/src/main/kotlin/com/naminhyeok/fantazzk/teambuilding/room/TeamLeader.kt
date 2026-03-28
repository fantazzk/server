package com.naminhyeok.fantazzk.teambuilding.room

data class TeamLeader(
    val id: TeamLeaderId,
    val nickname: String,
    val remainingBudget: Int? = null,
    val team: List<Player> = emptyList(),
) {
    fun hasPickedEnough(picksPerTeam: Int): Boolean = team.size >= picksPerTeam

    fun addPlayer(player: Player): TeamLeader = copy(team = team + player)

    fun deductBudget(amount: Int): TeamLeader {
        val current = requireNotNull(remainingBudget) { "No budget in this mode" }
        require(amount <= current) { "Insufficient budget: $current < $amount" }
        return copy(remainingBudget = current - amount)
    }
}
