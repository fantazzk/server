package com.naminhyeok.fantazzk.teambuilding.model.template

data class PlayerEntry(
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
)
