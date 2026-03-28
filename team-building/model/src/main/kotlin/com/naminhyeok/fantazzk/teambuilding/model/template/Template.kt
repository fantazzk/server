package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class Template(
    val id: TemplateId,
    val name: String,
    val mode: TeamBuildingMode,
    val rules: Rules,
    val players: List<PlayerEntry>,
)
