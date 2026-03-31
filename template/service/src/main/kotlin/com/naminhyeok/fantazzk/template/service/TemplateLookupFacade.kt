package com.naminhyeok.fantazzk.template.service

import com.naminhyeok.fantazzk.template.api.TemplateDraftStrategy
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateLookupException
import com.naminhyeok.fantazzk.template.api.TemplateMode
import com.naminhyeok.fantazzk.template.api.TemplatePlayerView
import com.naminhyeok.fantazzk.template.api.TemplateView
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.of
import com.naminhyeok.fantazzk.template.model.requireValidRoster

internal class TemplateLookupFacade(
    private val templateLookupService: TemplateLookupService,
) : TemplateLookup {
    override fun get(templateId: Long): TemplateView {
        try {
            val template =
                templateLookupService.find(TemplateIdentity.of(templateId))
                    ?: throw TemplateLookupException.NotFound(templateId)
            val players = templateLookupService.getPlayers(templateId).sortedBy { it.displayOrder }
            template.requireValidRoster(players)

            return TemplateView(
                templateId = template.templateId,
                mode = TemplateMode.valueOf(template.mode.name),
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy?.let { TemplateDraftStrategy.valueOf(it.name) },
                players = players.map { TemplatePlayerView(name = it.name, displayOrder = it.displayOrder) },
            )
        } catch (_: IllegalArgumentException) {
            throw TemplateLookupException.Invalid(templateId)
        }
    }
}
