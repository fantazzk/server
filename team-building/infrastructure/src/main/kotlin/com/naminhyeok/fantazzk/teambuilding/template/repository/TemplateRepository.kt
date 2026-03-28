package com.naminhyeok.fantazzk.teambuilding.template.repository

import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateIdentity
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel

interface TemplateRepository {
    fun save(template: Template): TemplateModel

    fun findById(identity: TemplateIdentity): TemplateModel?

    fun findAll(): List<TemplateModel>
}
