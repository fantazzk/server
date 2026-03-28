package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.Template

data class TemplateResponse(
    val id: Long,
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val playerCount: Int,
) {
    companion object {
        fun from(template: Template): TemplateResponse =
            TemplateResponse(
                id = template.id.value,
                name = template.name,
                mode = template.mode,
                teamCount = template.rules.teamCount,
                teamSize = template.rules.teamSize,
                playerCount = template.players.size,
            )
    }
}
