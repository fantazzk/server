package com.naminhyeok.fantazzk.template

interface TemplateModel : TemplateIdentity, TemplateProps, AuditProps

val Template.configuration: TemplateConfiguration
    get() = TemplateConfiguration.from(mode, teamCount, teamSize, budget, draftOrderStrategy)

fun Template.requireValidRoster(players: List<TemplatePlayer>) {
    val orderedPlayerNames = players.sortedBy { it.displayOrder }.map { it.name }
    TemplateRoster.exactlyRequired(orderedPlayerNames, configuration.requiredPlayerCount)
}

val Template.picksPerTeam: Int get() = teamSize - 1

val TemplateModel.configuration: TemplateConfiguration
    get() = TemplateConfiguration.from(mode, teamCount, teamSize, budget, draftOrderStrategy)

fun TemplateModel.requireValidRoster(players: List<TemplatePlayerModel>) {
    val orderedPlayerNames = players.sortedBy { it.displayOrder }.map { it.name }
    TemplateRoster.exactlyRequired(orderedPlayerNames, configuration.requiredPlayerCount)
}

val TemplateModel.picksPerTeam: Int get() = teamSize - 1
