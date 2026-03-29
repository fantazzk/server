package com.naminhyeok.fantazzk.bootstrap.root.adapter

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.outport.TemplateFetcher
import com.naminhyeok.fantazzk.room.outport.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.room.outport.TemplateSnapshot
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateLookUpService
import com.naminhyeok.fantazzk.template.of

class TemplateFetcherAdapter(
    private val templateLookUpService: TemplateLookUpService,
) : TemplateFetcher {
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
