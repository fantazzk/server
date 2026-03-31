package com.naminhyeok.fantazzk.template.service

import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.TemplateModel
import com.naminhyeok.fantazzk.template.model.TemplatePlayerModel
import com.naminhyeok.fantazzk.template.model.requireValidRoster

data class TemplateDetail(
    val template: TemplateModel,
    val players: List<TemplatePlayerModel>,
)

interface TemplateLookupService {
    fun get(identity: TemplateIdentity): TemplateModel

    fun find(identity: TemplateIdentity): TemplateModel?

    fun getAll(): List<TemplateModel>

    fun getPlayers(templateId: Long): List<TemplatePlayerModel>

    fun getDetail(identity: TemplateIdentity): TemplateDetail
}

internal class TemplateLookupServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookupService {
    override fun get(identity: TemplateIdentity): TemplateModel =
        try {
            find(identity) ?: throw TemplateException.TemplateNotFoundException()
        } catch (_: IllegalArgumentException) {
            throw TemplateException.TemplateInvalidException()
        }

    override fun find(identity: TemplateIdentity): TemplateModel? = templateRepository.findById(identity)

    override fun getAll(): List<TemplateModel> = templateRepository.findAll()

    override fun getPlayers(templateId: Long): List<TemplatePlayerModel> = templatePlayerRepository.findByTemplateId(templateId)

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
