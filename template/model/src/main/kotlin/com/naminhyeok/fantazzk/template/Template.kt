package com.naminhyeok.fantazzk.template

import java.time.Instant

data class Template(
    override val templateId: Long = 0L,
    override val name: String,
    override val mode: TeamBuildingMode,
    override val teamCount: Int,
    override val teamSize: Int,
    override val budget: Int? = null,
    override val draftOrderStrategy: DraftOrderStrategy? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now(),
) : TemplateModel {
    init {
        require(teamCount > 0) { "팀 수는 0보다 커야 합니다" }
        require(teamSize > 0) { "팀 크기는 0보다 커야 합니다" }
        budget?.let { require(it > 0) { "예산은 0보다 커야 합니다" } }
    }
}
