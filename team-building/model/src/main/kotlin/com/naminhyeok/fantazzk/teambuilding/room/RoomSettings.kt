package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

data class RoomSettings(
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    val picksPerTeam: Int get() = teamSize - 1

    init {
        require(teamCount > 0) { "teamCount must be positive" }
        require(teamSize > 0) { "teamSize must be positive" }
        budget?.let { require(it > 0) { "budget must be positive" } }
    }
}
