package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

interface TemplateProps {
    val name: String
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int
    val budget: Int?
    val draftOrderStrategy: DraftOrderStrategy?
}
