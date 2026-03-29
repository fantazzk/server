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

class TemplateLookupAdapter(
    private val templateLookupService: TemplateLookupService,
) : TemplateLookupPort {
    override fun getTemplate(templateId: Long): TemplateSnapshot {
        val template =
            try {
                templateLookupService.get(TemplateIdentity.of(templateId))
            } catch (ex: RuntimeException) {
                if (ex::class.qualifiedName == TEMPLATE_NOT_FOUND_EXCEPTION_CLASS_NAME) {
                    throw TemplateLookupPortException.NotFound(templateId)
                }
                throw ex
            }
        val players = templateLookupService.getPlayers(template.templateId)
        return TemplateSnapshot(
            mode = TeamBuildingMode.valueOf(template.mode.name),
            teamCount = template.teamCount,
            teamSize = template.teamSize,
            budget = template.budget,
            draftOrderStrategy = template.draftOrderStrategy?.let { DraftOrderStrategy.valueOf(it.name) },
            players = players.map { TemplatePlayerSnapshot(name = it.name, displayOrder = it.displayOrder) },
        )
    }

    private companion object {
        private const val TEMPLATE_NOT_FOUND_EXCEPTION_CLASS_NAME =
            "com.naminhyeok.fantazzk.template.exception.TemplateNotFoundException"
    }
}
