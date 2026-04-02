package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.domain.Template
import org.jmolecules.ddd.annotation.Repository
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

@Repository
interface TemplateRepository : JpaRepository<Template, Long> {
    fun findById(templateId: TemplateId): Optional<Template> = findById(templateId.value)

    override fun findAll(): List<Template>
}
