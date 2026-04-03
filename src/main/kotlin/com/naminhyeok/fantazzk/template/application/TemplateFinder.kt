package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.TemplateId
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

@org.jmolecules.ddd.annotation.Service
@Service
class TemplateFinder(
    private val templateRepository: TemplateRepository,
) {
    @Transactional(readOnly = true)
    fun getDetail(templateId: TemplateId): TemplateDetail {
        try {
            val template = templateRepository.findById(templateId) ?: throw TemplateException.TemplateNotFoundException()
            val players = template.players()
            template.requireValidRoster(players)
            return TemplateDetail(template, players)
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        } catch (_: InvalidDataAccessApiUsageException) {
            throw TemplateException.TemplateInvalidException()
        }
    }

    @Transactional(readOnly = true)
    fun list(): List<Template> =
        try {
            templateRepository.findAll().onEach { it.players() }
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        } catch (_: InvalidDataAccessApiUsageException) {
            throw TemplateException.TemplateInvalidException()
        }
}
