package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel

data class TemplateResponse(
    val id: Long,
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: DraftOrderStrategy?,
    val players: List<TemplatePlayerResponse>?,
) {
    companion object {
        fun from(
            template: TemplateModel,
            players: List<TemplatePlayerModel>? = null,
        ): TemplateResponse =
            TemplateResponse(
                id = template.templateId,
                name = template.name,
                mode = template.mode,
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy,
                players = players?.map { TemplatePlayerResponse(it.name, it.displayOrder) },
            )
    }
}
