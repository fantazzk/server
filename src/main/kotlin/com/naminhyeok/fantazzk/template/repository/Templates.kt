package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.domain.Template
import org.jmolecules.ddd.types.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.jmolecules.ddd.annotation.Repository as DddRepository

@DddRepository
interface Templates : Repository<Template, TemplateId> {
    fun save(template: Template): Template

    fun findById(templateId: TemplateId): Template?

    fun findAll(): List<Template>
}

internal interface TemplateJpaStore : JpaRepository<Template, Long>, Templates {
    override fun save(template: Template): Template

    override fun findAll(): List<Template>

    override fun findById(templateId: TemplateId): Template? = findById(templateId.value).orElse(null)
}
