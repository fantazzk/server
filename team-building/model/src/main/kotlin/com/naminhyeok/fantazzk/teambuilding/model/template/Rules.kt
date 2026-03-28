package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy

data class Rules(
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    init {
        require(teamCount > 0) { "teamCount must be positive" }
        require(teamSize > 0) { "teamSize must be positive" }
        budget?.let { require(it > 0) { "budget must be positive" } }
    }
}
