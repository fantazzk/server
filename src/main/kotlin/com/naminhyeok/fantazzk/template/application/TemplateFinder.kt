package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import com.naminhyeok.fantazzk.template.requireValidRoster
import org.springframework.stereotype.Service

data class TemplateDetail(
    val template: Template,
    val players: List<TemplatePlayer>,
)

interface TemplateFinder {
    fun getDetail(templateId: TemplateId): TemplateDetail

    fun list(): List<Template>
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class TemplateFinderImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateFinder {
    override fun getDetail(templateId: TemplateId): TemplateDetail {
        try {
            val template = templateRepository.findById(templateId) ?: throw TemplateException.TemplateNotFoundException()
            val players = templatePlayerRepository.findByTemplateId(template.templateId).sortedBy { it.displayOrder }
            template.requireValidRoster(players)
            return TemplateDetail(template, players)
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }
    }

    override fun list(): List<Template> =
        try {
            templateRepository.findAll()
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }
}
