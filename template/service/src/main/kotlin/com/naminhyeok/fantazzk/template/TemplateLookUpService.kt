package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

interface TemplateLookupService {
    fun get(identity: TemplateIdentity): TemplateModel

    fun find(identity: TemplateIdentity): TemplateModel?

    fun getAll(): List<TemplateModel>

    fun getPlayers(templateId: Long): List<TemplatePlayerModel>
}

internal class TemplateLookupServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookupService {
    override fun get(identity: TemplateIdentity): TemplateModel {
        val template = find(identity) ?: throw TemplateException.TemplateNotFoundException()
        if (!template.hasValidConfiguration()) {
            throw TemplateException.TemplateInvalidException()
        }
        return template
    }

    override fun find(identity: TemplateIdentity): TemplateModel? = templateRepository.findById(identity)

    override fun getAll(): List<TemplateModel> = templateRepository.findAll().filter { it.hasValidConfiguration() }

    override fun getPlayers(templateId: Long): List<TemplatePlayerModel> = templatePlayerRepository.findByTemplateId(templateId)
}
