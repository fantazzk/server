package com.naminhyeok.fantazzk.teambuilding.dto

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode

data class CreateTemplateRequest(
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
    val players: List<PlayerEntryRequest>,
)

data class PlayerEntryRequest(
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
)
