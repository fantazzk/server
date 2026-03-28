package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

data class Template(
    val id: TemplateId,
    val name: String,
    val mode: TeamBuildingMode,
    val rules: Rules,
    val players: List<PlayerEntry>,
)
