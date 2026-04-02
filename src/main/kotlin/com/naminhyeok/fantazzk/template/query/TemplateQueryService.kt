package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.exception.TemplateException
import org.springframework.stereotype.Service

data class TemplateView(
    val id: Long,
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int?,
    val draftOrderStrategy: DraftOrderStrategy?,
    val players: List<TemplatePlayerView>?,
)

data class TemplatePlayerView(
    val name: String,
    val displayOrder: Int,
)

interface TemplateQueryService {
    fun getTemplate(templateId: TemplateId): TemplateView

    fun listTemplates(): List<TemplateView>
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class TemplateQueryServiceImpl(
    private val templateViewProjectionRepository: TemplateViewProjectionRepository,
    private val templatePlayerViewProjectionRepository: TemplatePlayerViewProjectionRepository,
) : TemplateQueryService {
    override fun getTemplate(templateId: TemplateId): TemplateView {
        val template = templateViewProjectionRepository.findById(templateId.value) ?: throw TemplateException.TemplateNotFoundException()
        val players = templatePlayerViewProjectionRepository.findByTemplateIdOrderByDisplayOrder(templateId.value)

        return TemplateView(
            id = template.templateId,
            name = template.name,
            mode = template.mode,
            teamCount = template.teamCount,
            teamSize = template.teamSize,
            budget = template.budget,
            draftOrderStrategy = template.draftOrderStrategy,
            players = players.map { TemplatePlayerView(name = it.name, displayOrder = it.displayOrder) },
        )
    }

    override fun listTemplates(): List<TemplateView> =
        templateViewProjectionRepository.findAll().map {
            TemplateView(
                id = it.templateId,
                name = it.name,
                mode = it.mode,
                teamCount = it.teamCount,
                teamSize = it.teamSize,
                budget = it.budget,
                draftOrderStrategy = it.draftOrderStrategy,
                players = null,
            )
        }
}
