package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.AuditProps
import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateModel
import com.naminhyeok.fantazzk.template.TemplateProps
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface TemplateJdbcCrudRepository : CrudRepository<TemplateEntity, Long>

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
                createdAt = template.createdAt,
                updatedAt = template.updatedAt,
            )
        if (template.templateId != 0L) entity.id = template.templateId
        return templateJdbcCrudRepository.save(entity).toModel()
    }

    override fun findById(identity: TemplateIdentity): TemplateModel? =
        templateJdbcCrudRepository.findById(identity.templateId).orElse(null)?.toModel()

    override fun findAll(): List<TemplateModel> = templateJdbcCrudRepository.findAll().map { it.toModel() }

    private fun TemplateEntity.toModel() =
        PersistedTemplate(
            templateId = id,
            name = name,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = budget,
            draftOrderStrategy = draftOrderStrategy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

private data class PersistedTemplate(
    override val templateId: Long,
    override val name: String,
    override val mode: TeamBuildingMode,
    override val teamCount: Int,
    override val teamSize: Int,
    override val budget: Int?,
    override val draftOrderStrategy: DraftOrderStrategy?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : TemplateModel, TemplateProps, AuditProps
