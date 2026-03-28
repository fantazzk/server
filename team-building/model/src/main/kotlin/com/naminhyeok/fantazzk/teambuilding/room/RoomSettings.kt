package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.Rules

data class RoomSettings(
    val mode: TeamBuildingMode,
    val rules: Rules,
) {
    val teamCount: Int get() = rules.teamCount
    val teamSize: Int get() = rules.teamSize
    val budget: Int? get() = rules.budget
    val draftOrderStrategy: DraftOrderStrategy? get() = rules.draftOrderStrategy
    val picksPerTeam: Int get() = rules.teamSize - 1
}
