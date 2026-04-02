package com.naminhyeok.fantazzk.template.support

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository

class InMemoryTemplateRepository : TemplateRepository {
    private val store = mutableMapOf<Long, Template>()
    private var seq = 1L

    override fun save(template: Template): Template {
        val saved = if (template.templateId == 0L) template.copy(templateId = seq++) else template
        store[saved.templateId] = saved
        return saved
    }

    override fun findById(templateId: TemplateId): Template? = store[templateId.value]

    override fun findAll(): List<Template> = store.values.toList()
}

class InMemoryTemplatePlayerRepository : TemplatePlayerRepository {
    private val store = mutableListOf<TemplatePlayer>()
    private var seq = 1L

    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayer> {
        val saved = players.map { if (it.templatePlayerId == 0L) it.copy(templatePlayerId = seq++) else it }
        store.addAll(saved)
        return saved
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayer> = store.filter { it.templateId == templateId }.sortedBy { it.displayOrder }
}
