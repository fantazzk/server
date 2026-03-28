package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

data class Template(
    override val templateId: Long = 0L,
    override val name: String,
    override val mode: TeamBuildingMode,
    override val teamCount: Int,
    override val teamSize: Int,
    override val budget: Int? = null,
    override val draftOrderStrategy: DraftOrderStrategy? = null,
) : TemplateModel {
    init {
        require(teamCount > 0) { "팀 수는 1 이상이어야 합니다" }
        require(teamSize > 0) { "팀 인원은 1 이상이어야 합니다" }
        budget?.let { require(it > 0) { "예산은 1 이상이어야 합니다" } }
    }
}
