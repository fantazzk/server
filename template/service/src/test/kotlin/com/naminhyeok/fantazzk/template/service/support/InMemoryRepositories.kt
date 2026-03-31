package com.naminhyeok.fantazzk.template.service.support

import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.template.model.Template
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.TemplateModel
import com.naminhyeok.fantazzk.template.model.TemplatePlayer
import com.naminhyeok.fantazzk.template.model.TemplatePlayerModel

class InMemoryTemplateRepository : TemplateRepository {
    private val store = mutableMapOf<Long, Template>()
    private var seq = 1L

    override fun save(template: Template): TemplateModel {
        val saved = if (template.templateId == 0L) template.copy(templateId = seq++) else template
        store[saved.templateId] = saved
        return saved
    }

    override fun findById(identity: TemplateIdentity): TemplateModel? = store[identity.templateId]

    override fun findAll(): List<TemplateModel> = store.values.toList()
}

class InMemoryTemplatePlayerRepository : TemplatePlayerRepository {
    private val store = mutableListOf<TemplatePlayer>()
    private var seq = 1L

    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel> {
        val saved = players.map { if (it.templatePlayerId == 0L) it.copy(templatePlayerId = seq++) else it }
        store.addAll(saved)
        return saved
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayerModel> = store.filter { it.templateId == templateId }
}
