package com.naminhyeok.fantazzk.template

interface TemplateCatalog {
    fun getTemplateBlueprint(templateId: TemplateId): TemplateBlueprint
}
