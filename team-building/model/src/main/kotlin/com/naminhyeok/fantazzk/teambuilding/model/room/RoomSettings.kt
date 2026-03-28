package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class RoomSettings(
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    /** teamSize는 팀장 포함 인원. 팀장이 픽할 선수 수. */
    val picksPerTeam: Int get() = teamSize - 1
}
