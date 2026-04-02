package com.naminhyeok.fantazzk.template.domain

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class TemplateConfiguration protected constructor(
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    var mode: TeamBuildingMode,
    @Column(name = "team_count", nullable = false)
    var teamCount: Int,
    @Column(name = "team_size", nullable = false)
    var teamSize: Int,
    @Column(name = "budget")
    var budget: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy")
    var draftOrderStrategy: DraftOrderStrategy? = null,
) {
    init {
        require(teamCount > 0) { "팀 수는 0보다 커야 합니다" }
        require(teamSize > 0) { "팀 크기는 0보다 커야 합니다" }

        when (mode) {
            TeamBuildingMode.AUCTION -> {
                val currentBudget = requireNotNull(budget) { "경매 템플릿에는 예산이 필요합니다" }
                require(currentBudget > 0) { "예산은 0보다 커야 합니다" }
                require(draftOrderStrategy == null) { "경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다" }
            }

            TeamBuildingMode.DRAFT -> {
                require(budget == null) { "드래프트 템플릿에는 예산을 지정할 수 없습니다" }
                requireNotNull(draftOrderStrategy) { "드래프트 템플릿에는 순서 전략이 필요합니다" }
            }
        }
    }

    val requiredPlayerCount: Int
        get() = teamCount * (teamSize - 1)

    companion object {
        fun auction(
            teamCount: Int,
            teamSize: Int,
            budget: Int,
        ): TemplateConfiguration =
            TemplateConfiguration(
                mode = TeamBuildingMode.AUCTION,
                teamCount = teamCount,
                teamSize = teamSize,
                budget = budget,
            )

        fun draft(
            teamCount: Int,
            teamSize: Int,
            strategy: DraftOrderStrategy,
        ): TemplateConfiguration =
            TemplateConfiguration(
                mode = TeamBuildingMode.DRAFT,
                teamCount = teamCount,
                teamSize = teamSize,
                draftOrderStrategy = strategy,
            )
    }
}
