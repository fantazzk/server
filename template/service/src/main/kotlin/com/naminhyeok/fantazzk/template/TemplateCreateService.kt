package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

interface TemplateCreateService {
    fun create(
        name: String,
        mode: TeamBuildingMode,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: DraftOrderStrategy?,
        playerNames: List<String>,
    ): TemplateModel
}

internal class TemplateCreateServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateCreateService {
    override fun create(
        name: String,
        mode: TeamBuildingMode,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: DraftOrderStrategy?,
        playerNames: List<String>,
    ): TemplateModel {
        val template =
            templateRepository.save(
                Template(
                    name = name,
                    mode = mode,
                    teamCount = teamCount,
                    teamSize = teamSize,
                    budget = budget,
                    draftOrderStrategy = draftOrderStrategy,
                ),
            )

        templatePlayerRepository.saveAll(
            playerNames.mapIndexed { index, playerName ->
                TemplatePlayer(templateId = template.templateId, name = playerName, displayOrder = index)
            },
        )

        return template
    }
}
