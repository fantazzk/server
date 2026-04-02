package com.naminhyeok.fantazzk.template

sealed interface TemplateConfiguration {
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int
    val budget: Int?
    val draftOrderStrategy: DraftOrderStrategy?
    val requiredPlayerCount: Int

    data class Auction(
        override val teamCount: Int,
        override val teamSize: Int,
        val budgetValue: Int,
    ) : TemplateConfiguration {
        init {
            require(teamCount > 0) { "팀 수는 0보다 커야 합니다" }
            require(teamSize > 0) { "팀 크기는 0보다 커야 합니다" }
            require(budgetValue > 0) { "예산은 0보다 커야 합니다" }
        }

        override val mode: TeamBuildingMode = TeamBuildingMode.AUCTION
        override val budget: Int = budgetValue
        override val draftOrderStrategy: DraftOrderStrategy? = null
        override val requiredPlayerCount: Int = teamCount * (teamSize - 1)
    }

    data class Draft(
        override val teamCount: Int,
        override val teamSize: Int,
        val strategy: DraftOrderStrategy,
    ) : TemplateConfiguration {
        init {
            require(teamCount > 0) { "팀 수는 0보다 커야 합니다" }
            require(teamSize > 0) { "팀 크기는 0보다 커야 합니다" }
        }

        override val mode: TeamBuildingMode = TeamBuildingMode.DRAFT
        override val budget: Int? = null
        override val draftOrderStrategy: DraftOrderStrategy = strategy
        override val requiredPlayerCount: Int = teamCount * (teamSize - 1)
    }

    companion object {
        fun from(
            mode: TeamBuildingMode,
            teamCount: Int,
            teamSize: Int,
            budget: Int?,
            draftOrderStrategy: DraftOrderStrategy?,
        ): TemplateConfiguration =
            when (mode) {
                TeamBuildingMode.AUCTION -> {
                    require(draftOrderStrategy == null) { "경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다" }
                    Auction(
                        teamCount = teamCount,
                        teamSize = teamSize,
                        budgetValue = requireNotNull(budget) { "경매 템플릿에는 예산이 필요합니다" },
                    )
                }

                TeamBuildingMode.DRAFT -> {
                    require(budget == null) { "드래프트 템플릿에는 예산을 지정할 수 없습니다" }
                    Draft(
                        teamCount = teamCount,
                        teamSize = teamSize,
                        strategy = requireNotNull(draftOrderStrategy) { "드래프트 템플릿에는 순서 전략이 필요합니다" },
                    )
                }
            }
    }
}
