package com.naminhyeok.fantazzk.template.repository.jdbc

import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.model.TemplatePlayer
import com.naminhyeok.fantazzk.template.model.TemplatePlayerModel
import org.springframework.data.repository.CrudRepository

interface TemplatePlayerJdbcCrudRepository : CrudRepository<TemplatePlayerEntity, Long> {
    fun findByTemplateIdOrderByDisplayOrderAsc(templateId: Long): List<TemplatePlayerEntity>
}

class TemplatePlayerRepositoryImpl(
    private val templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository,
) : TemplatePlayerRepository {
    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel> {
        val entities =
            players.map { player ->
                val entity =
                    TemplatePlayerEntity(
                        templateId = player.templateId,
                        name = player.name,
                        displayOrder = player.displayOrder,
                        createdAt = player.createdAt,
                        updatedAt = player.updatedAt,
                    )
                if (player.templatePlayerId != 0L) entity.id = player.templatePlayerId
                entity
            }
        return templatePlayerJdbcCrudRepository.saveAll(entities).map { it.toModel() }
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayerModel> =
        templatePlayerJdbcCrudRepository.findByTemplateIdOrderByDisplayOrderAsc(templateId).map { it.toModel() }

    private fun TemplatePlayerEntity.toModel() =
        TemplatePlayer(
            templatePlayerId = id,
            templateId = templateId,
            name = name,
            displayOrder = displayOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
