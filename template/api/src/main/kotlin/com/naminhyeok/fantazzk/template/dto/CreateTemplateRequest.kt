package com.naminhyeok.fantazzk.template.dto

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode

data class CreateTemplateRequest(
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
    val playerNames: List<String>,
)
