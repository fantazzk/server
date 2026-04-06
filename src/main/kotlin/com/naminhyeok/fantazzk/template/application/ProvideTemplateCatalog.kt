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
    override fun getTemplateBlueprint(templateId: TemplateId): TemplateBlueprint =
        try {
            val detail = templateFinder.getDetail(templateId)
            TemplateBlueprint(
                templateId,
                TemplateMode.valueOf(detail.template.mode.name),
                detail.template.teamCount,
                detail.template.teamSize,
                detail.template.budget,
                detail.template.draftOrderStrategy?.let {
                    TemplateDraftOrderStrategy.valueOf(it.name)
                },
                detail.players.map {
                    TemplatePlayerBlueprint(
                        it.name,
                        it.displayOrder,
                    )
                },
            )
        } catch (_: TemplateException.TemplateNotFoundException) {
            throw TemplateCatalogException.NotFound(templateId)
        } catch (_: TemplateException.TemplateInvalidException) {
            throw TemplateCatalogException.Invalid(templateId)
        }
}
