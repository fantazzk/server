package com.naminhyeok.fantazzk.template.infrastructure

import com.naminhyeok.fantazzk.template.model.Template
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.TemplateModel

interface TemplateRepository {
    fun save(template: Template): TemplateModel

    fun findById(identity: TemplateIdentity): TemplateModel?

    fun findAll(): List<TemplateModel>
}
