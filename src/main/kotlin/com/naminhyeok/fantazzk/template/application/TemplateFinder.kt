package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateRoster
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
) : TemplateFinder {
    @Transactional(readOnly = true)
    override fun getDetail(templateId: TemplateId): TemplateDetail {
        try {
            val template = templateRepository.findById(templateId).orElse(null) ?: throw TemplateException.TemplateNotFoundException()
            val players = template.players()
            TemplateRoster.exactlyRequired(players.map { it.name }, template.configuration.requiredPlayerCount)
            return TemplateDetail(template, players)
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        } catch (_: InvalidDataAccessApiUsageException) {
            throw TemplateException.TemplateInvalidException()
        }
    }

    @Transactional(readOnly = true)
    override fun list(): List<Template> =
        try {
            templateRepository.findAll()
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        } catch (_: InvalidDataAccessApiUsageException) {
            throw TemplateException.TemplateInvalidException()
        }
}
