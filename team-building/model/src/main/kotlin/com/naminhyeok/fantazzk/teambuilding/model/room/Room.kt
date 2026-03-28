package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class Room(
    val id: RoomId,
    val code: String,
    val hostId: TeamLeaderId,
    val status: RoomStatus,
    val settings: RoomSettings,
    val playerPool: PlayerPool,
    val teamLeaders: List<TeamLeader>,
    val progression: Progression?,
    val result: RoomResult?,
) {
    fun addTeamLeader(id: TeamLeaderId, nickname: String): Room {
        check(status == RoomStatus.WAITING) { "Can only add team leaders while WAITING" }
        check(teamLeaders.size < settings.teamCount) { "Room is full" }
        check(teamLeaders.none { it.id == id }) { "Team leader already joined" }

        val leader = TeamLeader(
            id = id,
            nickname = nickname,
            remainingBudget = settings.budget,
        )
        return copy(teamLeaders = teamLeaders + leader)
    }

    fun start(): Room {
        check(status == RoomStatus.WAITING) { "Can only start from WAITING" }
        check(teamLeaders.size == settings.teamCount) { "All team leader slots must be filled" }

        val progression = when (settings.mode) {
            TeamBuildingMode.AUCTION -> Progression.Auction()
            TeamBuildingMode.DRAFT -> {
                val strategy = requireNotNull(settings.draftOrderStrategy) { "Draft requires order strategy" }
                val pickOrder = Progression.Draft.generatePickOrder(
                    teamLeaders.map { it.id },
                    strategy,
                    settings.picksPerTeam,
                )
                Progression.Draft(pickOrder = pickOrder)
            }
        }

        return copy(status = RoomStatus.IN_PROGRESS, progression = progression)
    }

    companion object {
        fun create(
            id: RoomId,
            code: String,
            hostId: TeamLeaderId,
            hostNickname: String,
            settings: RoomSettings,
            playerPool: PlayerPool,
        ): Room {
            val host = TeamLeader(
                id = hostId,
                nickname = hostNickname,
                remainingBudget = settings.budget,
            )
            return Room(
                id = id,
                code = code,
                hostId = hostId,
                status = RoomStatus.WAITING,
                settings = settings,
                playerPool = playerPool,
                teamLeaders = listOf(host),
                progression = null,
                result = null,
            )
        }
    }
}
