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
        val current = requireNotNull(remainingBudget) { "이 모드에서는 예산이 존재하지 않습니다" }
        require(amount <= current) { "예산이 부족합니다: 잔여 $current, 필요 $amount" }
        return copy(remainingBudget = current - amount)
    }
}
