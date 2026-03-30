package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

sealed interface CreateTemplateCommand {
    val name: String
    val teamCount: Int
    val teamSize: Int
    val playerNames: List<String>

    data class Auction(
        override val name: String,
        override val teamCount: Int,
        override val teamSize: Int,
        val budget: Int,
        override val playerNames: List<String>,
    ) : CreateTemplateCommand

    data class Draft(
        override val name: String,
        override val teamCount: Int,
        override val teamSize: Int,
        val strategy: DraftOrderStrategy,
        override val playerNames: List<String>,
    ) : CreateTemplateCommand
}

interface TemplateCreateService {
    fun create(command: CreateTemplateCommand): TemplateModel
}

internal class TemplateCreateServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateCreateService {
    override fun create(command: CreateTemplateCommand): TemplateModel {
        val configuration =
            when (command) {
                is CreateTemplateCommand.Auction ->
                    TemplateConfiguration.Auction(
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        budgetValue = command.budget,
                    )

                is CreateTemplateCommand.Draft ->
                    TemplateConfiguration.Draft(
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        strategy = command.strategy,
                    )
            }
        val roster = TemplateRoster.exactlyRequired(command.playerNames, configuration.requiredPlayerCount)
        val template =
            templateRepository.save(
                Template.create(
                    name = command.name,
                    configuration = configuration,
                ),
            )

        templatePlayerRepository.saveAll(roster.toPlayers(template.templateId))

        return template
    }
}
