package com.naminhyeok.fantazzk.template.infrastructure.spi

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.spi.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateLookupException
import com.naminhyeok.fantazzk.template.spi.TemplateMode
import com.naminhyeok.fantazzk.template.spi.TemplatePlayerSnapshot
import com.naminhyeok.fantazzk.template.spi.TemplateSnapshot

internal class TemplateLookupAdapter(
    private val templateFinder: TemplateFinder,
) : TemplateLookup {
    override fun getTemplate(templateId: Long): TemplateSnapshot {
        try {
            val detail = templateFinder.getDetail(TemplateId(templateId))
            val template = detail.template
            val players = detail.players

            return TemplateSnapshot(
                mode = TemplateMode.valueOf(template.mode.name),
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy?.let { TemplateDraftOrderStrategy.valueOf(it.name) },
                players = players.map { TemplatePlayerSnapshot(name = it.name, displayOrder = it.displayOrder) },
            )
        } catch (_: TemplateException.TemplateNotFoundException) {
            throw TemplateLookupException.NotFound(templateId)
        } catch (_: TemplateException.TemplateInvalidException) {
            throw TemplateLookupException.Invalid(templateId)
        }
    }
}
