package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateId
import org.jmolecules.ddd.annotation.Repository

@Repository
interface TemplateRepository {
    fun save(template: Template): Template

    fun findById(templateId: TemplateId): Template?

    fun findAll(): List<Template>
}
