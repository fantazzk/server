package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId

interface TemplateRepository {
    fun save(template: Template): Template

    fun findById(id: TemplateId): Template?

    fun findAll(): List<Template>
}
