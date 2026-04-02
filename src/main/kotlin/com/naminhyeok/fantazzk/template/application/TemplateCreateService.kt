package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateRoster
import com.naminhyeok.fantazzk.template.domain.Template
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
    private val events: ApplicationEventPublisher,
) : TemplateCreateService {
    @Transactional
    override fun create(command: CreateTemplateCommand): Template {
        val sortedPlayers = command.playerNames.sorted()
        val requiredPlayerCount = command.teamCount * (command.teamSize - 1)
        TemplateRoster.exactlyRequired(sortedPlayers, requiredPlayerCount)

        val template =
            when (command) {
                is CreateTemplateCommand.Auction ->
                    Template.createAuction(
                        name = command.name,
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        budget = command.budget,
                        playerNames = sortedPlayers,
                    )

                is CreateTemplateCommand.Draft ->
                    Template.createDraft(
                        name = command.name,
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        strategy = command.strategy,
                        playerNames = sortedPlayers,
                    )
            }
        val saved = templateRepository.save(template)
        saved.recordCreated().drainEvents().forEach(events::publishEvent)

        return saved
    }
}
