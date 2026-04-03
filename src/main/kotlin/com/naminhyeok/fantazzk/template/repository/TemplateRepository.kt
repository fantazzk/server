package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.domain.Template
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.repository.JpaRepository

@Repository
interface TemplateRepository {
    fun save(template: Template): Template

    fun findById(templateId: TemplateId): Template?

    fun findAll(): List<Template>
}

internal interface TemplateJpaStore : JpaRepository<Template, Long>, TemplateRepository {
    override fun save(template: Template): Template

    override fun findAll(): List<Template>

    override fun findById(templateId: TemplateId): Template? = findById(templateId.value).orElse(null)
}
