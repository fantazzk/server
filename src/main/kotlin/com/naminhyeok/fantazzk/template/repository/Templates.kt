package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.domain.Template
import org.jmolecules.ddd.types.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.jmolecules.ddd.annotation.Repository as DddRepository

@DddRepository
interface Templates : Repository<Template, TemplateId> {
    fun save(template: Template): Template

    fun findById(templateId: TemplateId): Template?

    fun findAll(): List<Template>
}

internal interface TemplateJpaStore : JpaRepository<Template, TemplateId> {
    override fun <S : Template> save(entity: S): S

    override fun findAll(): List<Template>
}

@Component
class TemplateRepositoryAdapter internal constructor(
    private val store: TemplateJpaStore,
) : Templates {
    override fun save(template: Template): Template = store.save(template)

    override fun findById(templateId: TemplateId): Template? = store.findById(templateId).orElse(null)

    override fun findAll(): List<Template> = store.findAll()
}
