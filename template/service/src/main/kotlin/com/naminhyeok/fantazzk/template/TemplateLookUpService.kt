package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

interface TemplateLookupService {
    fun get(identity: TemplateIdentity): TemplateModel

    fun getAll(): List<TemplateModel>

    fun getPlayers(templateId: Long): List<TemplatePlayerModel>
}

internal class TemplateLookupServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookupService {
    override fun get(identity: TemplateIdentity): TemplateModel =
        templateRepository.findById(identity) ?: throw TemplateException.TemplateNotFoundException()

    override fun getAll(): List<TemplateModel> = templateRepository.findAll()

    override fun getPlayers(templateId: Long): List<TemplatePlayerModel> = templatePlayerRepository.findByTemplateId(templateId)
}
