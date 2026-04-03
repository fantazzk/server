package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.repository.Templates
import org.springframework.stereotype.Component
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

@Component
class CreateTemplate(
    private val templateRepository: Templates,
) {
    @Transactional
    fun create(command: CreateTemplateCommand): Template {
        val template =
            when (command) {
                is CreateTemplateCommand.Auction ->
                    Template.createAuction(
                        name = command.name,
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        budget = command.budget,
                        playerNames = command.playerNames,
                    )

                is CreateTemplateCommand.Draft ->
                    Template.createDraft(
                        name = command.name,
                        teamCount = command.teamCount,
                        teamSize = command.teamSize,
                        strategy = command.strategy,
                        playerNames = command.playerNames,
                    )
            }
        return templateRepository.save(template)
    }
}
