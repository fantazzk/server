package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplateModel

interface TemplateRepository {
    fun save(template: Template): TemplateModel

    fun findById(identity: TemplateIdentity): TemplateModel?

    fun findAll(): List<TemplateModel>
}
