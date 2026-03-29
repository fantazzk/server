package com.naminhyeok.fantazzk.integration.roomtemplate

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.room.outport.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.room.outport.TemplateSnapshot
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateLookUpService
import com.naminhyeok.fantazzk.template.of

class TemplateLookupAdapter(
    private val templateLookUpService: TemplateLookUpService,
) : TemplateLookupPort {
    override fun getTemplate(templateId: Long): TemplateSnapshot {
        val template = templateLookUpService.get(TemplateIdentity.of(templateId))
        return TemplateSnapshot(
            mode = TeamBuildingMode.valueOf(template.mode.name),
            teamCount = template.teamCount,
            teamSize = template.teamSize,
            budget = template.budget,
            draftOrderStrategy = template.draftOrderStrategy?.let { DraftOrderStrategy.valueOf(it.name) },
        )
    }

    override fun getPlayers(templateId: Long): List<TemplatePlayerSnapshot> =
        templateLookUpService.getPlayers(templateId).map { player ->
            TemplatePlayerSnapshot(
                name = player.name,
                displayOrder = player.displayOrder,
            )
        }
}
