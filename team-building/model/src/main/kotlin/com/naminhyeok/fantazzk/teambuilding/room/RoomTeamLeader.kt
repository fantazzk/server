package com.naminhyeok.fantazzk.teambuilding.room

data class RoomTeamLeader(
    override val roomTeamLeaderId: Long = 0L,
    override val roomId: Long,
    override val teamLeaderId: String,
    override val nickname: String,
    override val remainingBudget: Int? = null,
) : RoomTeamLeaderModel {
    fun validateBudget(amount: Int) {
        val current = requireNotNull(remainingBudget) { "이 모드에서는 예산이 존재하지 않습니다" }
        require(amount <= current) { "예산이 부족합니다: 잔여 $current, 필요 $amount" }
    }

    companion object {
        fun from(model: RoomTeamLeaderModel): RoomTeamLeader =
            RoomTeamLeader(
                roomTeamLeaderId = model.roomTeamLeaderId,
                roomId = model.roomId,
                teamLeaderId = model.teamLeaderId,
                nickname = model.nickname,
                remainingBudget = model.remainingBudget,
            )
    }
}
