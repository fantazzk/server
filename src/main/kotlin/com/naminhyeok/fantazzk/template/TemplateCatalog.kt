package com.naminhyeok.fantazzk.template

interface TemplateCatalog {
    fun get(templateId: TemplateId): TemplateBlueprint
}
