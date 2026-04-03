package com.naminhyeok.fantazzk.template

class TemplateRoster private constructor(
    private val names: List<String>,
) {
    fun playerNames(): List<String> = names.toList()

    companion object {
        fun exactlyRequired(
            playerNames: List<String>,
            requiredPlayerCount: Int,
        ): TemplateRoster {
            require(playerNames.size == requiredPlayerCount) { "선수 수는 정확히 ${requiredPlayerCount}명이어야 합니다" }
            return TemplateRoster(playerNames.toList())
        }
    }
}
