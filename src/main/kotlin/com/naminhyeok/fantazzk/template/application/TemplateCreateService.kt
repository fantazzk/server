package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateConfiguration
import com.naminhyeok.fantazzk.template.TemplateRoster
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun create(command: CreateTemplateCommand): Template
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class TemplateCreateServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
    private val events: ApplicationEventPublisher,
) : TemplateCreateService {
    @Transactional
    override fun create(command: CreateTemplateCommand): Template {
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

        val players = templatePlayerRepository.saveAll(roster.toPlayers(template.templateId))
        template.recordCreated(players).drainEvents().forEach(events::publishEvent)

        return template
    }
}
