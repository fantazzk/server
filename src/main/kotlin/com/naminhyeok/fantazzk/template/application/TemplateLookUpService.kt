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

interface TemplateLookupService {
    fun get(templateId: TemplateId): Template

    fun find(templateId: TemplateId): Template?

    fun getAll(): List<Template>

    fun getPlayers(templateId: Long): List<TemplatePlayer>

    fun getDetail(templateId: TemplateId): TemplateDetail
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class TemplateLookupServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookupService {
    override fun get(templateId: TemplateId): Template =
        try {
            find(templateId) ?: throw TemplateException.TemplateNotFoundException()
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }

    override fun find(templateId: TemplateId): Template? = templateRepository.findById(templateId)

    override fun getAll(): List<Template> = templateRepository.findAll()

    override fun getPlayers(templateId: Long): List<TemplatePlayer> = templatePlayerRepository.findByTemplateId(templateId)

    override fun getDetail(templateId: TemplateId): TemplateDetail {
        val template = get(templateId)
        val players = getPlayers(template.templateId)
        try {
            template.requireValidRoster(players)
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }
        return TemplateDetail(template, players)
    }
}
