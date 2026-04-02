package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplatePlayer
import org.springframework.data.repository.CrudRepository

interface TemplatePlayerJdbcCrudRepository : CrudRepository<TemplatePlayerEntity, Long> {
    fun findByTemplateIdOrderByDisplayOrderAsc(templateId: Long): List<TemplatePlayerEntity>
}

class TemplatePlayerRepositoryImpl(
    private val templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository,
) : TemplatePlayerRepository {
    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayer> {
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
        return templatePlayerJdbcCrudRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayer> =
        templatePlayerJdbcCrudRepository.findByTemplateIdOrderByDisplayOrderAsc(templateId).map { it.toDomain() }

    private fun TemplatePlayerEntity.toDomain() =
        TemplatePlayer(
            templatePlayerId = id,
            templateId = templateId,
            name = name,
            displayOrder = displayOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
