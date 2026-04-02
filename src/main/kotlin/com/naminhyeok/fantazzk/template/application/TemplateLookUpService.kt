package com.naminhyeok.fantazzk.template.application

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateIdentity
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
    fun get(identity: TemplateIdentity): Template

    fun find(identity: TemplateIdentity): Template?

    fun getAll(): List<Template>

    fun getPlayers(templateId: Long): List<TemplatePlayer>

    fun getDetail(identity: TemplateIdentity): TemplateDetail
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class TemplateLookupServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookupService {
    override fun get(identity: TemplateIdentity): Template =
        try {
            find(identity) ?: throw TemplateException.TemplateNotFoundException()
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }

    override fun find(identity: TemplateIdentity): Template? = templateRepository.findById(identity)

    override fun getAll(): List<Template> = templateRepository.findAll()

    override fun getPlayers(templateId: Long): List<TemplatePlayer> = templatePlayerRepository.findByTemplateId(templateId)

    override fun getDetail(identity: TemplateIdentity): TemplateDetail {
        val template = get(identity)
        val players = getPlayers(template.templateId)
        try {
            template.requireValidRoster(players)
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }
        return TemplateDetail(template, players)
    }
}
