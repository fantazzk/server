package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import com.naminhyeok.fantazzk.teambuilding.repository.TemplateRepository
import com.naminhyeok.fantazzk.teambuilding.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId

interface TemplateService {
    fun create(
        name: String,
        mode: TeamBuildingMode,
        rules: Rules,
        players: List<PlayerEntry>,
    ): Template

    fun get(id: TemplateId): Template

    fun getAll(): List<Template>
}

internal class TemplateServiceImpl(
    private val templateRepository: TemplateRepository,
) : TemplateService {
    override fun create(
        name: String,
        mode: TeamBuildingMode,
        rules: Rules,
        players: List<PlayerEntry>,
    ): Template {
        val template =
            Template(
                id = TemplateId(0L),
                name = name,
                mode = mode,
                rules = rules,
                players = players,
            )
        return templateRepository.save(template)
    }

    override fun get(id: TemplateId): Template = templateRepository.findById(id) ?: throw TemplateNotFoundException()

    override fun getAll(): List<Template> = templateRepository.findAll()
}
