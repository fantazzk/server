package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import com.naminhyeok.fantazzk.teambuilding.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.teambuilding.template.repository.TemplateRepository

interface TemplateLookUpService {
    fun get(identity: TemplateIdentity): TemplateModel

    fun getAll(): List<TemplateModel>

    fun getPlayers(templateId: Long): List<TemplatePlayerModel>
}

internal class TemplateLookUpServiceImpl(
    private val templateRepository: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
) : TemplateLookUpService {
    override fun get(identity: TemplateIdentity): TemplateModel = templateRepository.findById(identity) ?: throw TemplateNotFoundException()

    override fun getAll(): List<TemplateModel> = templateRepository.findAll()

    override fun getPlayers(templateId: Long): List<TemplatePlayerModel> = templatePlayerRepository.findByTemplateId(templateId)
}
