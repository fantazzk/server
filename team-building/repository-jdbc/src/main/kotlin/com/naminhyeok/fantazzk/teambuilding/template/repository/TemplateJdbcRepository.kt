package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateIdentity
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayer
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel
import org.springframework.data.repository.CrudRepository

interface TemplateJdbcCrudRepository : CrudRepository<TemplateEntity, Long>

interface TemplatePlayerJdbcCrudRepository : CrudRepository<TemplatePlayerEntity, Long> {
    fun findByTemplateId(templateId: Long): List<TemplatePlayerEntity>
}

class TemplateRepositoryImpl(
    private val templateJdbcCrudRepository: TemplateJdbcCrudRepository,
) : TemplateRepository {
    override fun save(template: Template): TemplateModel {
        val entity =
            TemplateEntity(
                name = template.name,
                mode = template.mode,
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy,
            )
        if (template.templateId != 0L) entity.id = template.templateId
        return templateJdbcCrudRepository.save(entity).toModel()
    }

    override fun findById(identity: TemplateIdentity): TemplateModel? =
        templateJdbcCrudRepository.findById(identity.templateId).orElse(null)?.toModel()

    override fun findAll(): List<TemplateModel> = templateJdbcCrudRepository.findAll().map { it.toModel() }

    private fun TemplateEntity.toModel() =
        Template(
            templateId = id,
            name = name,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = budget,
            draftOrderStrategy = draftOrderStrategy,
        )
}

class TemplatePlayerRepositoryImpl(
    private val templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository,
) : TemplatePlayerRepository {
    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel> {
        val entities =
            players.map { player ->
                val entity = TemplatePlayerEntity(templateId = player.templateId, name = player.name, displayOrder = player.displayOrder)
                if (player.templatePlayerId != 0L) entity.id = player.templatePlayerId
                entity
            }
        return templatePlayerJdbcCrudRepository.saveAll(entities).map { it.toModel() }
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayerModel> =
        templatePlayerJdbcCrudRepository.findByTemplateId(templateId).map { it.toModel() }

    private fun TemplatePlayerEntity.toModel() =
        TemplatePlayer(
            templatePlayerId = id,
            templateId = templateId,
            name = name,
            displayOrder = displayOrder,
        )
}
