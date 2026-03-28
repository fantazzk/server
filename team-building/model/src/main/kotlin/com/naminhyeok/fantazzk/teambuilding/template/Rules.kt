package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy

data class Rules(
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    init {
        require(teamCount > 0) { "팀 수는 1 이상이어야 합니다" }
        require(teamSize > 0) { "팀 인원은 1 이상이어야 합니다" }
        budget?.let { require(it > 0) { "예산은 1 이상이어야 합니다" } }
    }
}
