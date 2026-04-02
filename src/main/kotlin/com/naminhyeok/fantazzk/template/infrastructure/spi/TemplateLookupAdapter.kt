package com.naminhyeok.fantazzk.template.infrastructure.spi

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.application.TemplateLookupService
import com.naminhyeok.fantazzk.template.requireValidRoster
import com.naminhyeok.fantazzk.template.spi.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateLookupException
import com.naminhyeok.fantazzk.template.spi.TemplateMode
import com.naminhyeok.fantazzk.template.spi.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.template.spi.TemplateSnapshot

internal class TemplateLookupAdapter(
    private val templateLookupService: TemplateLookupService,
) : TemplateLookup {
    override fun getTemplate(templateId: Long): TemplateSnapshot {
        try {
            val template =
                templateLookupService.find(TemplateId(templateId))
                    ?: throw TemplateLookupException.NotFound(templateId)
            val players = templateLookupService.getPlayers(template.templateId).sortedBy { it.displayOrder }
            template.requireValidRoster(players)

            return TemplateSnapshot(
                mode = TemplateMode.valueOf(template.mode.name),
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy?.let { TemplateDraftOrderStrategy.valueOf(it.name) },
                players = players.map { TemplatePlayerSnapshot(name = it.name, displayOrder = it.displayOrder) },
            )
        } catch (_: IllegalArgumentException) {
            throw TemplateLookupException.Invalid(templateId)
        }
    }
}
