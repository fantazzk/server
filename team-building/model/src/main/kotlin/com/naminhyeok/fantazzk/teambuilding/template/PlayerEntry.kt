package com.naminhyeok.fantazzk.teambuilding.template

data class PlayerEntry(
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
)
