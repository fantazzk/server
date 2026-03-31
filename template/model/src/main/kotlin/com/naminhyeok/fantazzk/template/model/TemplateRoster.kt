package com.naminhyeok.fantazzk.template.model

class TemplateRoster private constructor(
    private val names: List<String>,
) {
    fun playerNames(): List<String> = names.toList()

    fun toPlayers(templateId: Long): List<TemplatePlayer> =
        names.mapIndexed { index, playerName ->
            TemplatePlayer(templateId = templateId, name = playerName, displayOrder = index)
        }

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
