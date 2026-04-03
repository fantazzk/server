package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateMode
import com.naminhyeok.fantazzk.template.TemplatePlayerBlueprint
import com.naminhyeok.fantazzk.template.exception.TemplateException
import org.springframework.stereotype.Component

@Component
internal class ProvideTemplateCatalog(
    private val templateFinder: FindTemplates,
) : TemplateCatalog {
    override fun getTemplateBlueprint(templateId: Long): TemplateBlueprint =
        try {
            val detail = templateFinder.getDetail(TemplateId(templateId))
            TemplateBlueprint(
                templateId = templateId,
                mode = TemplateMode.valueOf(detail.template.mode.name),
                teamCount = detail.template.teamCount,
                teamSize = detail.template.teamSize,
                budget = detail.template.budget,
                draftOrderStrategy =
                    detail.template.draftOrderStrategy?.let {
                        TemplateDraftOrderStrategy.valueOf(it.name)
                    },
                players =
                    detail.players.map {
                        TemplatePlayerBlueprint(
                            name = it.name,
                            displayOrder = it.displayOrder,
                        )
                    },
            )
        } catch (_: TemplateException.TemplateNotFoundException) {
            throw TemplateCatalogException.NotFound(templateId)
        } catch (_: TemplateException.TemplateInvalidException) {
            throw TemplateCatalogException.Invalid(templateId)
        }
}
