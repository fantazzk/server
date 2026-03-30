package com.naminhyeok.fantazzk.integration.roomtemplate

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPortException
import com.naminhyeok.fantazzk.room.outport.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.room.outport.TemplateSnapshot
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateLookupService
import com.naminhyeok.fantazzk.template.of
import com.naminhyeok.fantazzk.template.requireValidRoster

class TemplateLookupAdapter(
    private val templateLookupService: TemplateLookupService,
) : TemplateLookupPort {
    override fun getTemplate(templateId: Long): TemplateSnapshot {
        try {
            val template =
                templateLookupService.find(TemplateIdentity.of(templateId))
                    ?: throw TemplateLookupPortException.NotFound(templateId)
            val players = templateLookupService.getPlayers(template.templateId).sortedBy { it.displayOrder }
            template.requireValidRoster(players)

            return TemplateSnapshot(
                mode = TeamBuildingMode.valueOf(template.mode.name),
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy?.let { DraftOrderStrategy.valueOf(it.name) },
                players = players.map { TemplatePlayerSnapshot(name = it.name, displayOrder = it.displayOrder) },
            )
        } catch (_: IllegalArgumentException) {
            throw TemplateLookupPortException.Invalid(templateId)
        }
    }
}
