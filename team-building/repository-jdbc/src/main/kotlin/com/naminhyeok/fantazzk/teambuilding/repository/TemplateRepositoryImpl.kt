package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId
import tools.jackson.databind.ObjectMapper

internal class TemplateRepositoryImpl(
    private val templateJdbcRepository: TemplateJdbcRepository,
    private val objectMapper: ObjectMapper,
) : TemplateRepository {
    override fun save(template: Template): Template {
        val entity = toEntity(template)
        val saved = templateJdbcRepository.save(entity)
        return toModel(saved)
    }

    override fun findById(id: TemplateId): Template? = templateJdbcRepository.findById(id.value).orElse(null)?.let(::toModel)

    override fun findAll(): List<Template> = templateJdbcRepository.findAll().map(::toModel)

    private fun toEntity(template: Template): TemplateEntity {
        val entity =
            TemplateEntity(
                name = template.name,
                mode = template.mode.name,
                rulesJson = objectMapper.writeValueAsString(template.rules),
                playersJson = objectMapper.writeValueAsString(template.players),
            )
        if (template.id.value != 0L) {
            entity.id = template.id.value
        }
        return entity
    }

    private fun toModel(entity: TemplateEntity): Template =
        Template(
            id = TemplateId(entity.id),
            name = entity.name,
            mode = TeamBuildingMode.valueOf(entity.mode),
            rules = objectMapper.readValue(entity.rulesJson, Rules::class.java),
            players =
                objectMapper.readValue(
                    entity.playersJson,
                    objectMapper.typeFactory.constructCollectionType(List::class.java, PlayerEntry::class.java),
                ),
        )
}
