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
        require(teamCount > 0) { "팀 수는 1 이상이어야 합니다" }
        require(teamSize > 0) { "팀 인원은 1 이상이어야 합니다" }
        budget?.let { require(it > 0) { "예산은 1 이상이어야 합니다" } }
    }
}
