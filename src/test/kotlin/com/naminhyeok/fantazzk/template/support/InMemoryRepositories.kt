package com.naminhyeok.fantazzk.template.support

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateModel
import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.TemplatePlayerModel
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

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
